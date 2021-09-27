package org.openvasp.client.session.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import org.openvasp.client.common.VaspUtils;
import org.openvasp.client.model.*;
import org.openvasp.client.service.ContractService;
import org.openvasp.client.service.MessageService;
import org.openvasp.client.service.TopicEvent;
import org.openvasp.client.session.Session;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

/**
 * @author Olexandr_Bilovol@epam.com
 */
abstract class AbstractSession implements Session {

    final SessionManagerImpl owner;

    @Getter
    @Setter
    private BiConsumer<VaspMessage, Session> messageHandler;

    private final String sessionId;

    String sharedSecret;

    Topic topicA;
    Topic topicB;

    TransferInfo transferInfo;
    VaspInfo peerVaspInfo;

    private final Queue<VaspMessage> incomingQueue = new LinkedList<>();
    private final Lock incomingQueueLock = new ReentrantLock();
    private final Condition hasNewMessages = incomingQueueLock.newCondition();

    AbstractSession(@NonNull final SessionManagerImpl owner, @NonNull final String sessionId) {
        this.owner = owner;
        this.sessionId = sessionId;
    }

    ContractService contractService() {
        return owner.contractService;
    }

    MessageService messageService() {
        return owner.messageService;
    }

    @Override
    public String sessionId() {
        return sessionId;
    }

    @Override
    public VaspInfo vaspInfo() {
        return owner.vaspInfo;
    }

    @Override
    public VaspInfo peerVaspInfo() {
        return peerVaspInfo;
    }

    @Override
    public TransferInfo transferInfo() {
        return transferInfo;
    }

    abstract Topic incomingMessageTopic();

    abstract Topic outgoingMessageTopic();

    @Override
    public void sendMessage(@NonNull final VaspMessage message) {
        message.getHeader().setMessageId(VaspUtils.newMessageId());
        message.getHeader().setSessionId(sessionId);

        messageService().send(
                outgoingMessageTopic(),
                EncryptionType.SYMMETRIC,
                sharedSecret,
                message);
    }

    void addToIncomingQueue(@NonNull final VaspMessage message) {
        incomingQueueLock.lock();
        try {
            incomingQueue.add(message);
            hasNewMessages.signalAll();
        } finally {
            incomingQueueLock.unlock();
        }
    }

    @Override
    public Optional<VaspMessage> takeIncomingMessage(long timeout) {
        incomingQueueLock.lock();
        try {
            if (!incomingQueue.isEmpty()) {
                return Optional.of(incomingQueue.remove());
            } else {
                return hasNewMessages.await(timeout, TimeUnit.MILLISECONDS)
                        ? Optional.of(incomingQueue.remove())
                        : Optional.empty();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } finally {
            incomingQueueLock.unlock();
        }
    }

    void onReceiveMessage(@NonNull final TopicEvent<VaspMessage> event) {
        if (messageHandler != null) {
            // Process the message immediately
            messageHandler.accept(event.getPayload(), this);
        } else {
            // Put the message into incomingQueue for further processing via takeIncomingMessage
            addToIncomingQueue(event.getPayload());
        }
    }

    @Override
    public SessionState getState() {
        val builder = SessionState.builder();
        buildState(builder);
        return builder.build();
    }

    void buildState(final SessionState.SessionStateBuilder builder) {
        builder.id(sessionId)
                .incomingTopic(incomingMessageTopic())
                .outgoingTopic(outgoingMessageTopic())
                .sharedSecret(sharedSecret)
                .transferInfo(transferInfo)
                .peerVaspInfo(peerVaspInfo);
    }

}
