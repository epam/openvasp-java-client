package org.openvasp.client.service.impl;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.openvasp.client.api.whisper.WhisperApi;
import org.openvasp.client.api.whisper.WhisperIOException;
import org.openvasp.client.api.whisper.model.ShhMessage;
import org.openvasp.client.api.whisper.model.ShhNewMessageFilterRequest;
import org.openvasp.client.api.whisper.model.ShhPostRequest;
import org.openvasp.client.common.ExceptionHandler;
import org.openvasp.client.common.Tuple2;
import org.openvasp.client.common.VaspException;
import org.openvasp.client.config.VaspConfig;
import org.openvasp.client.model.EncryptionType;
import org.openvasp.client.model.Topic;
import org.openvasp.client.service.TopicEvent;
import org.openvasp.client.service.TopicListener;
import org.openvasp.client.service.WhisperService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.InterruptedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Singleton
@Slf4j
public final class WhisperServiceImpl implements WhisperService {

    private static final BigInteger TTL = BigInteger.valueOf(60);
    private static final BigInteger POW_TIME = BigInteger.valueOf(20);
    private static final BigDecimal POW_TARGET = BigDecimal.valueOf(2.01);

    private static final long POLL_TIMEOUT = 1000;

    private static final int RUNNING = 1;
    private static final int SHUTDOWN = 2;
    private static final int TERMINATED = 3;

    private final WhisperApi whisper;

    private final Thread pollingThread;
    private final AtomicInteger state = new AtomicInteger(RUNNING);
    private final ReentrantLock stateCtl = new ReentrantLock();
    private final Condition termination = stateCtl.newCondition();

    private final ConcurrentMap<Topic, TopicListenerRecord> listenerRecords = new ConcurrentHashMap<>();
    private final Lock listenerRecordsLock = new ReentrantLock();

    private final ExceptionHandler exceptionHandler;

    @Inject
    public WhisperServiceImpl(
            final VaspConfig vaspConfig,
            final ExceptionHandler exceptionHandler,
            final WhisperApi whisper) {

        this.exceptionHandler = exceptionHandler;
        this.whisper = whisper;
        this.pollingThread = new Thread(
                this::topicPollingLoop,
                "TopicPollingLoop-" + vaspConfig.getVaspInfo().getVaspCode());
        pollingThread.start();
    }

    @Override
    public void close() {
        if (!checkState(TERMINATED)) {
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

    public void shutdown() {
        stateCtl.lock();
        try {
            state.compareAndSet(RUNNING, SHUTDOWN);
        } finally {
            stateCtl.unlock();
        }
    }

    public boolean waitForTermination(final long msTimeout) {
        stateCtl.lock();
        try {
            return state.get() == TERMINATED || termination.await(msTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            stateCtl.unlock();
        }
    }

    @Override
    public void send(
            @NonNull final Topic topic,
            @NonNull final EncryptionType encType,
            @NonNull final String key,
            @NonNull final String payload) {

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

    @Override
    public long addTopicListener(
            @NonNull final Topic topic,
            @NonNull final EncryptionType encType,
            @NonNull final String key,
            @NonNull final TopicListener<ShhMessage> listener) {

        listenerRecordsLock.lock();
        try {
            TopicListenerRecord listenerRecord = listenerRecords.get(topic);
            if (listenerRecord == null) {
                listenerRecord = new TopicListenerRecord(topic, encType, key);
                listenerRecords.put(topic, listenerRecord);
            }
            return listenerRecord.addTopicListener(listener);
        } finally {
            listenerRecordsLock.unlock();
        }
    }

    @Override
    public void removeTopicListener(
            @NonNull final Topic topic,
            long listenerId) {

        listenerRecordsLock.lock();
        try {
            val listenerRecord = listenerRecords.get(topic);
            if (listenerRecord != null) {
                listenerRecord.removeTopicListener(listenerId);
                if (listenerRecord.isEmpty()) {
                    listenerRecords.remove(topic);
                    listenerRecord.close();
                }
            }
        } finally {
            listenerRecordsLock.unlock();
        }
    }

    private Tuple2<String, String> createWhisperMessageFilter(
            @NonNull final Topic topic,
            @NonNull final EncryptionType encType,
            @NonNull final String key) {

        final String keyId, filterId;
        switch (encType) {
            case ASSYMETRIC: {
                keyId = whisper.addPrivateKey(key);
                filterId = whisper.newMessageFilter(ShhNewMessageFilterRequest.builder()
                        .privateKeyId(keyId)
                        .topics(Collections.singletonList(topic.getData()))
                        .build());
                break;
            }

            case SYMMETRIC: {
                keyId = whisper.addSymKey(key);
                filterId = whisper.newMessageFilter(ShhNewMessageFilterRequest.builder()
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

        return Tuple2.of(keyId, filterId);
    }

    private void deleteWhisperMessageFilter(
            @NonNull final EncryptionType encType,
            @NonNull final String keyId,
            @NonNull final String filterId) {

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

    private Stream<ShhMessage> getWhisperMessages(@NonNull final String filterId) {
        return whisper.getFilterMessages(filterId).stream();
    }

    private void topicPollingLoop() {
        log.debug("{} started", Thread.currentThread().getName());
        try {
            while (!Thread.interrupted() && checkState(RUNNING)) {
                pollMessages();
                Thread.sleep(POLL_TIMEOUT);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (WhisperIOException ex) {
            val cause = ex.getCause();
            if (!(cause instanceof InterruptedIOException)) {
                log.error(formatErrorMessage(), ex);
            }
        } catch (Exception ex) {
            log.error(formatErrorMessage(), ex);
        } finally {
            setTerminatedState();
            log.debug("{} terminated", Thread.currentThread().getName());
        }
    }

    private void pollMessages() {
        final List<ShhMessage> incomingMessages;

        listenerRecordsLock.lock();
        try {
            incomingMessages = listenerRecords.values()
                    .stream()
                    .map(TopicListenerRecord::getFilterId)
                    .flatMap(this::getWhisperMessages)
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
        try {
            val listenerRecord = listenerRecords.get(topic);
            if (listenerRecord != null) {
                listenerRecord.onTopicEvent(new TopicEvent<>(topic, whisperMessage));
            }
        } catch (WhisperIOException ex) {
            throw ex;
        } catch (VaspException ex) {
            log.error("Error of processing a Whisper incoming message at the topic " + topic, ex);
            exceptionHandler.processException(ex);
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

    private void setTerminatedState() {
        stateCtl.lock();
        try {
            state.set(TERMINATED);
            termination.signalAll();
        } finally {
            stateCtl.unlock();
        }
    }

    private String formatErrorMessage() {
        return String.format("Error in %s.topicPollingLoop", getClass().getSimpleName());
    }

    private static final AtomicLong nextTopicListenerId = new AtomicLong(0L);

    @Getter
    private class TopicListenerRecord implements AutoCloseable, TopicListener<ShhMessage> {

        final Topic topic;
        final EncryptionType encType;
        final String key;
        String keyId;
        String filterId;
        private final Map<Long, TopicListener<ShhMessage>> topicListeners = new LinkedHashMap<>();

        TopicListenerRecord(
                @NonNull final Topic topic,
                @NonNull final EncryptionType encType,
                @NonNull final String key) {

            this.topic = topic;
            this.encType = encType;
            this.key = key;

            val keyFilterIds = createWhisperMessageFilter(topic, encType, key);
            this.keyId = keyFilterIds._1;
            this.filterId = keyFilterIds._2;
        }

        @Override
        public void close() {
            deleteWhisperMessageFilter(encType, keyId, filterId);
        }

        @Override
        public synchronized void onTopicEvent(@NonNull final TopicEvent<ShhMessage> event) {
            for (val listener : Lists.newArrayList(topicListeners.values())) {
                listener.onTopicEvent(event);
            }
        }

        synchronized long addTopicListener(@NonNull final TopicListener<ShhMessage> topicListener) {
            val topicListenerId = nextTopicListenerId.incrementAndGet();
            topicListeners.put(topicListenerId, topicListener);
            return topicListenerId;
        }

        synchronized void removeTopicListener(final long topicListenerId) {
            topicListeners.remove(topicListenerId);
        }

        synchronized boolean isEmpty() {
            return topicListeners.isEmpty();
        }

    }

}
