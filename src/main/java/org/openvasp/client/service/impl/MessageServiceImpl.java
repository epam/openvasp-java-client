package org.openvasp.client.service.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.openvasp.client.api.whisper.WhisperApi;
import org.openvasp.client.api.whisper.WhisperIOException;
import org.openvasp.client.api.whisper.model.ShhMessage;
import org.openvasp.client.api.whisper.model.ShhNewMessageFilterRequest;
import org.openvasp.client.api.whisper.model.ShhPostRequest;
import org.openvasp.client.common.Json;
import org.openvasp.client.common.VaspException;
import org.openvasp.client.common.VaspValidationException;
import org.openvasp.client.config.VaspConfig;
import org.openvasp.client.model.*;
import org.openvasp.client.service.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.InterruptedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.stream.Collectors.toList;
import static org.openvasp.client.common.Constants.SIGNATURE_LENGTH;
import static org.openvasp.client.common.VaspUtils.hexStrDecode;
import static org.openvasp.client.common.VaspUtils.hexStrEncode;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Singleton
@Slf4j
public final class MessageServiceImpl implements MessageService {

    private static final BigInteger TTL = BigInteger.valueOf(60);
    private static final BigInteger POW_TIME = BigInteger.valueOf(4);
    private static final BigDecimal POW_TARGET = BigDecimal.valueOf(2.01);

    private static final long POLL_TIMEOUT = 1000;

    private static final int RUNNING = 1;
    private static final int SHUTDOWN = 2;
    private static final int TERMINATED = 3;

    private final WhisperApi whisper;
    private final SignService signService;
    private final ContractService contractService;

    private final VaspCode senderVaspCode;
    private final String senderSigningPrivateKey;

    private final ConcurrentMap<Topic, TopicListenerRecord> listenerRecords = new ConcurrentHashMap<>();
    private final Lock listenerRecordsLock = new ReentrantLock();

    private final Thread pollingThread;
    private final AtomicInteger state = new AtomicInteger(RUNNING);
    private final ReentrantLock stateCtl = new ReentrantLock();
    private final Condition termination = stateCtl.newCondition();

    @Inject
    public MessageServiceImpl(
            final VaspConfig vaspConfig,
            final WhisperApi whisper,
            final SignService signService,
            final ContractService contractService) {

        this.whisper = whisper;
        this.signService = signService;
        this.contractService = contractService;

        this.senderVaspCode = vaspConfig.getVaspCode();
        this.senderSigningPrivateKey = vaspConfig.getSigningPrivateKey();

        this.pollingThread = new Thread(
                this::topicPollingLoop,
                "TopicPollingLoop-" + vaspConfig.getVaspCode());
        this.pollingThread.start();
    }

    @Override
    public void close() {
        if (state.get() != TERMINATED) {
            pollingThread.interrupt();
        }

        listenerRecordsLock.lock();
        try {
            listenerRecords.values().forEach(TopicListenerRecord::close);
            listenerRecords.clear();
        } finally {
            listenerRecordsLock.unlock();
        }
    }

    @Override
    public void shutdown() {
        compareAndSetState(RUNNING, SHUTDOWN);
    }

    @Override
    public boolean waitForTermination(final long timeout, @NonNull final TimeUnit unit) {
        stateCtl.lock();
        try {
            termination.await(timeout, unit);
            return true;
        } catch (InterruptedException ex) {
            return false;
        } finally {
            stateCtl.unlock();
        }
    }

    @Override
    public void addTopicListener(
            @NonNull final Topic topic,
            @NonNull final EncryptionType encType,
            @NonNull final String key,
            @NonNull final TopicListener listener) {

        listenerRecordsLock.lock();
        try {
            var listenerRecord = listenerRecords.get(topic);
            if (listenerRecord == null) {
                listenerRecord = new TopicListenerRecord(topic, encType, key);
                listenerRecords.put(topic, listenerRecord);
            }
            if (!listenerRecord.listeners.contains(listener)) {
                listenerRecord.listeners.add(listener);
            }
        } finally {
            listenerRecordsLock.unlock();
        }
    }

    @Override
    public void removeTopicListener(@NonNull final Topic topic, @NonNull final TopicListener listener) {
        listenerRecordsLock.lock();
        try {
            val listenerRecord = listenerRecords.get(topic);
            if (listenerRecord != null) {
                listenerRecord.listeners.remove(listener);
                if (listenerRecord.listeners.isEmpty()) {
                    listenerRecords.remove(topic);
                    listenerRecord.close();
                }
            }
        } finally {
            listenerRecordsLock.unlock();
        }
    }

    @Override
    public void removeTopicListeners(@NonNull final Topic topic) {
        listenerRecordsLock.lock();
        try {
            val listenerRecord = listenerRecords.get(topic);
            if (listenerRecord != null) {
                listenerRecords.remove(topic);
                listenerRecord.close();
            }
        } finally {
            listenerRecordsLock.unlock();
        }
    }

    @Override
    public void send(
            @NonNull final Topic topic,
            @NonNull final EncryptionType encType,
            @NonNull final String key,
            @NonNull final VaspMessage message) {

        val payload = makePayload(message);

        switch (encType) {
            case ASSYMETRIC: {
                val request = ShhPostRequest.builder()
                        .pubKey(key)
                        .topic(topic.getData())
                        .payload(payload)
                        .ttl(TTL)
                        .powTarget(POW_TARGET)
                        .powTime(POW_TIME)
                        .build();
                whisper.post(request);
                break;
            }

            case SYMMETRIC: {
                val symKeyId = whisper.addSymKey(key);
                val request = ShhPostRequest.builder()
                        .symKeyId(symKeyId)
                        .topic(topic.getData())
                        .payload(payload)
                        .ttl(TTL)
                        .powTarget(POW_TARGET)
                        .powTime(POW_TIME)
                        .build();
                whisper.post(request);
                break;
            }
        }
    }

    private String makePayload(@NonNull final VaspMessage message) {
        if (message.getVaspInfo() == null) {
            message.setVaspInfo(new VaspInfo());
        }
        message.getVaspInfo().setVaspCode(senderVaspCode);

        val json = Json.toJson(message);
        val signature = signService.signPayload(json, senderSigningPrivateKey);
        return hexStrEncode(json + signature, true);
    }

    private void topicPollingLoop() {
        log.debug("{} started", Thread.currentThread().getName());
        try {
            while (!Thread.interrupted() && checkState(RUNNING)) {
                pollMessages();
                Thread.sleep(POLL_TIMEOUT);
            }
        } catch (InterruptedException ex) {
            // Do nothing, just exit
        } catch (WhisperIOException ex) {
            val cause = ex.getCause();
            if (!(cause instanceof InterruptedIOException)) {
                log.error("Error in MessageServicePollingLoop", ex);
            }
        } catch (Exception ex) {
            log.error("Error in MessageServicePollingLoop", ex);
        } finally {
            setState(TERMINATED);
        }
    }

    private void pollMessages() {
        final List<ShhMessage> incomingMessages;

        listenerRecordsLock.lock();
        try {
            incomingMessages = listenerRecords.values().stream()
                    .flatMap(lr -> whisper.getFilterMessages(lr.filterId).stream())
                    .collect(toList());
        } finally {
            listenerRecordsLock.unlock();
        }

        for (val message : incomingMessages) {
            processIncomingMassage(message);
        }
    }

    private void processIncomingMassage(@NonNull final ShhMessage whisperMessage) {
        val topic = new Topic(whisperMessage.getTopic());
        val listenerRecord = listenerRecords.get(topic);
        if (listenerRecord == null) {
            return;
        }

        val payload = hexStrDecode(whisperMessage.getPayload());
        val json = StringUtils.left(payload, payload.length() - SIGNATURE_LENGTH);
        val signature = StringUtils.right(payload, SIGNATURE_LENGTH);

        val vaspMessage = VaspMessage.fromJson(json);
        val senderVaspCode = vaspMessage.getVaspInfo().getVaspCode();
        val senderContract = contractService.getVaspContractInfo(senderVaspCode);
        val publicSigningKey = senderContract.getSigningKey();

        if (validateIncomingMessage(vaspMessage, listenerRecord)) {
            if (!signService.verifySign(json, signature, publicSigningKey)) {
                val ex = new VaspException("Invalid signature for incoming message at topic %s", topic);
                listenerRecord.onError(new TopicErrorEvent(listenerRecord.topic, ex));
            }

            listenerRecord.onReceiveMessage(new TopicEvent(topic, vaspMessage));
        }
    }

    private boolean validateIncomingMessage(
            @NonNull final VaspMessage vaspMessage,
            @NonNull final TopicListenerRecord listenerRecord) {

        try {
            vaspMessage.validate();
            return true;
        } catch (VaspValidationException ex) {
            listenerRecord.onError(new TopicErrorEvent(listenerRecord.topic, ex));
            return false;
        }
    }

    private boolean checkState(final int expectedState) {
        stateCtl.lock();
        try {
            return state.get() == expectedState;
        } finally {
            stateCtl.unlock();
        }
    }

    private boolean setState(final int newState) {
        stateCtl.lock();
        try {
            if (state.getAndSet(newState) == newState) {
                return false;
            }
            if (newState == TERMINATED) {
                termination.signalAll();
            }
            return true;
        } finally {
            stateCtl.unlock();
        }
    }

    private boolean compareAndSetState(final int expectedState, final int newState) {
        stateCtl.lock();
        try {
            if (state.compareAndSet(expectedState, newState)) {
                if (newState == TERMINATED) {
                    termination.signalAll();
                }
                return true;
            }
            return false;
        } finally {
            stateCtl.unlock();
        }
    }

    @Getter
    private class TopicListenerRecord implements AutoCloseable, TopicListener {

        final Topic topic;
        final EncryptionType encType;
        final String key;
        String keyId;
        String filterId;
        final List<TopicListener> listeners = Collections.synchronizedList(new ArrayList<>());

        TopicListenerRecord(
                @NonNull final Topic topic,
                @NonNull final EncryptionType encType,
                @NonNull final String key) {

            this.topic = topic;
            this.encType = encType;
            this.key = key;

            switch (encType) {
                case ASSYMETRIC: {
                    this.keyId = whisper.addPrivateKey(key);
                    this.filterId = whisper.newMessageFilter(ShhNewMessageFilterRequest.builder()
                            .privateKeyId(keyId)
                            .topics(Collections.singletonList(topic.getData()))
                            .build());
                    break;
                }

                case SYMMETRIC: {
                    this.keyId = whisper.addSymKey(key);
                    this.filterId = whisper.newMessageFilter(ShhNewMessageFilterRequest.builder()
                            .symKeyId(keyId)
                            .topics(Collections.singletonList(topic.getData()))
                            .build());
                    break;
                }

                default:
                    // encType cannot be null, so it is impossible to get there
                    // But because the Java compiler does not know that, it requires
                    // initialization of final 'keyId' and 'filterId'.
                    // The exception is just a workaround for the situation
                    throw new VaspException("It's impossible to get here");
            }
        }

        @Override
        public void close() {
            if (filterId != null) {
                whisper.deleteMessageFilter(filterId);
            }

            if (keyId != null) {
                switch (encType) {
                    case ASSYMETRIC:
                        whisper.deleteKeyPair(keyId);
                        break;

                    case SYMMETRIC:
                        whisper.deleteSymKey(keyId);
                        break;
                }
            }
        }

        @Override
        public void onReceiveMessage(@NonNull final TopicEvent event) {
            for (val listener : listeners) {
                listener.onReceiveMessage(event);
            }
        }

        @Override
        public void onError(@NonNull final TopicErrorEvent event) {
            for (val listener : listeners) {
                listener.onError(event);
            }
        }

    }

}
