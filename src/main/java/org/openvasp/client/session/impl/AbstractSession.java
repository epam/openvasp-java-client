package org.openvasp.client.session.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.openvasp.client.common.VaspException;
import org.openvasp.client.common.VaspUtils;
import org.openvasp.client.crypto.ECDHKeyPair;
import org.openvasp.client.model.*;
import org.openvasp.client.service.*;
import org.openvasp.client.session.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

/**
 * @author Olexandr_Bilovol@epam.com
 */
abstract class AbstractSession implements Session, TopicListener {

    final VaspInstanceImpl owner;

    @Getter
    @Setter
    private BiConsumer<VaspMessage, Session> customMessageHandler;

    @Getter
    @Setter
    private BiConsumer<VaspException, Session> customErrorHandler;

    private final String sessionId;

    ECDHKeyPair sessionKeyPair;
    String sharedSecret;

    Topic topicA;
    Topic topicB;

    TransferInfo transferInfo;
    VaspInfo peerVaspInfo;

    private final ConcurrentMap<String, Object> attrs = new ConcurrentHashMap<>();

    private final List<VaspMessage> incomingMessages = Collections.synchronizedList(new ArrayList<>());
    private final Lock incomingMessagesLock = new ReentrantLock();
    private final Condition newMessageCondition = incomingMessagesLock.newCondition();

    private final List<VaspException> errors = Collections.synchronizedList(new ArrayList<>());

    AbstractSession(@NonNull final VaspInstanceImpl owner, @NonNull final String sessionId) {
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
    public VaspCode vaspCode() {
        return owner.vaspCode();
    }

    @Override
    public VaspInfo peerVaspInfo() {
        return peerVaspInfo;
    }

    @Override
    public TransferInfo transferInfo() {
        return transferInfo;
    }

    @Override
    public abstract VaspCode peerVaspCode();

    abstract String sharedSecret();

    abstract Topic incomingMessageTopic();

    abstract Topic outgoingMessageTopic();

    @Override
    public List<VaspMessage> incomingMessages() {
        return Collections.unmodifiableList(incomingMessages);
    }

    @Override
    public List<VaspException> errors() {
        return Collections.unmodifiableList(errors);
    }

    @Override
    public void sendMessage(@NonNull final VaspMessage message) {
        message.getHeader().setMessageId(VaspUtils.newMessageId());
        message.getHeader().setSessionId(sessionId);
        message.setVaspInfo(owner.vaspInfo);

        owner.messageService.send(
                outgoingMessageTopic(),
                EncryptionType.SYMMETRIC,
                sharedSecret(),
                message);
    }

    @Override
    public void onReceiveMessage(@NonNull final TopicEvent event) {
        addIncomingMessage(event.getMessage());
        if (customMessageHandler != null) {
            customMessageHandler.accept(event.getMessage(), this);
        }
    }

    @Override
    public void onError(@NonNull final TopicErrorEvent event) {
        errors.add(event.getCause());
        if (customErrorHandler != null) {
            customErrorHandler.accept(event.getCause(), this);
        }
    }

    void addIncomingMessage(@NonNull final VaspMessage message) {
        incomingMessagesLock.lock();
        try {
            incomingMessages.add(message);
            newMessageCondition.signalAll();
        } finally {
            incomingMessagesLock.unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends VaspMessage> Optional<T> waitForNewMessage(
            @NonNull final Class<T> messageClass,
            final long timeout) {

        incomingMessagesLock.lock();
        try {
            return newMessageCondition.await(timeout, TimeUnit.MILLISECONDS)
                    ? Optional.of((T) incomingMessages.get(incomingMessages.size() - 1))
                    : Optional.empty();
        } catch (InterruptedException ex) {
            return Optional.empty();
        } finally {
            incomingMessagesLock.unlock();
        }
    }

    @Override
    public Object getAttr(@NonNull String key) {
        return attrs.get(key);
    }

    @Override
    public Object putAttr(@NonNull String key, @NonNull Object value) {
        return attrs.put(key, value);
    }

    @Override
    public Object removeAttr(@NonNull String key) {
        return attrs.remove(key);
    }

}
