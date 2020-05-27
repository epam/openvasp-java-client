package org.openvasp.client.service.impl;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.openvasp.client.api.whisper.WhisperApi;
import org.openvasp.client.api.whisper.model.ShhMessage;
import org.openvasp.client.common.VaspValidationException;
import org.openvasp.client.config.VaspConfig;
import org.openvasp.client.model.EncryptionType;
import org.openvasp.client.model.Topic;
import org.openvasp.client.model.VaspInfo;
import org.openvasp.client.model.VaspMessage;
import org.openvasp.client.service.*;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.stream.Collectors.toList;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Singleton
@Slf4j
public final class MessageServiceImpl extends AbstractTopicService implements MessageService {

    private final SignService signService;

    private final VaspInfo senderVaspInfo;
    private final String senderSigningPrivateKey;

    private final ConcurrentMap<Topic, TopicListenerRecord> listenerRecords = new ConcurrentHashMap<>();
    private final Lock listenerRecordsLock = new ReentrantLock();

    private final ConfirmationService confirmationService;

    @Inject
    public MessageServiceImpl(
            final VaspConfig vaspConfig,
            final WhisperApi whisper,
            final SignService signService,
            final ConfirmationService confirmationService) {

        super(whisper, "PollingLoopM-" + vaspConfig.getVaspInfo().getVaspCode());

        this.signService = signService;
        this.confirmationService = confirmationService;

        this.senderVaspInfo = vaspConfig.getVaspInfo();
        this.senderSigningPrivateKey = vaspConfig.getSigningPrivateKey();

        startPolling();
    }

    @Override
    public void close() {
        super.close();

        listenerRecordsLock.lock();
        try {
            listenerRecords.values().forEach(TopicListenerRecord::close);
            listenerRecords.clear();
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

        confirmationService.registerForConfirmation(message);
        if (message.getVaspInfo() == null) {
            message.setVaspInfo(senderVaspInfo);
        }
        message.getVaspInfo().setVaspId(senderVaspInfo.getVaspId());
        super.send(topic, encType, key, signService.makeSignedPayload(message, senderSigningPrivateKey));
    }

    @Override
    public void addTopicListener(
            @NonNull final Topic topic,
            @NonNull final EncryptionType encType,
            @NonNull final String key,
            @NonNull final TopicListener listener) {

        listenerRecordsLock.lock();
        try {
            TopicListenerRecord listenerRecord = listenerRecords.get(topic);
            if (listenerRecord == null) {
                listenerRecord = new TopicListenerRecord(topic, encType, key);
                listenerRecords.put(topic, listenerRecord);
            }
            listenerRecord.addTopicListener(listener);
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
                listenerRecord.removeTopicListener(listener);
                if (listenerRecord.isEmpty()) {
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
    Logger log() {
        return log;
    }

    @Override
    void pollMessages() {
        final List<ShhMessage> incomingMessages;

        listenerRecordsLock.lock();
        try {
            incomingMessages = listenerRecords.values().stream()
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
        val listenerRecord = listenerRecords.get(topic);
        try {
            val vaspMessage = signService.extractSignedMessage(whisperMessage.getPayload());
            confirmationService.confirmReceipt(vaspMessage);
            if (listenerRecord != null) {
                listenerRecord.onReceiveMessage(new TopicEvent(topic, vaspMessage));
            }
        } catch (VaspValidationException ex) {
            if (listenerRecord != null) {
                listenerRecord.onError(new TopicErrorEvent(listenerRecord.topic, ex));
            }
        }
    }

    @Getter
    private class TopicListenerRecord implements AutoCloseable, TopicListener {

        final Topic topic;
        final EncryptionType encType;
        final String key;
        String keyId;
        String filterId;
        private final List<TopicListener> topicListeners = new ArrayList<>();

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
        public synchronized void onReceiveMessage(@NonNull final TopicEvent event) {
            for (val listener : Lists.newArrayList(topicListeners)) {
                listener.onReceiveMessage(event);
            }
        }

        @Override
        public synchronized void onError(@NonNull final TopicErrorEvent event) {
            for (val listener : Lists.newArrayList(topicListeners)) {
                listener.onError(event);
            }
        }

        synchronized void addTopicListener(@NonNull final TopicListener topicListener) {
            if (!topicListeners.contains(topicListener)) {
                topicListeners.add(topicListener);
            }
        }

        synchronized void removeTopicListener(@NonNull final TopicListener topicListener) {
            topicListeners.remove(topicListener);
        }

        synchronized boolean isEmpty() {
            return topicListeners.isEmpty();
        }

    }

}
