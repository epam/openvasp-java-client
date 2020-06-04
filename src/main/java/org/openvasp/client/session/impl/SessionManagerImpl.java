package org.openvasp.client.session.impl;

import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import org.openvasp.client.config.VaspConfig;
import org.openvasp.client.model.*;
import org.openvasp.client.service.ContractService;
import org.openvasp.client.service.MessageService;
import org.openvasp.client.service.TopicEvent;
import org.openvasp.client.session.BeneficiarySession;
import org.openvasp.client.session.OriginatorSession;
import org.openvasp.client.session.Session;
import org.openvasp.client.session.SessionManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
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
@Singleton
public final class SessionManagerImpl implements SessionManager {

    final ContractService contractService;
    final MessageService messageService;

    @Setter
    BiConsumer<VaspMessage, Session> messageHandler;

    final VaspInfo vaspInfo;
    final String handshakePrivateKey;

    private final ConcurrentMap<String, BeneficiarySessionImpl> beneficiarySessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, OriginatorSessionImpl> originatorSessions = new ConcurrentHashMap<>();

    private final Lock sessionsLock = new ReentrantLock();
    private final Condition newBeneficiarySession = sessionsLock.newCondition();
    private final Condition noActiveSessions = sessionsLock.newCondition();

    @Inject
    public SessionManagerImpl(
            final VaspConfig vaspConfig,
            final ContractService contractService,
            final MessageService messageService) {

        this.contractService = contractService;
        this.messageService = messageService;

        this.handshakePrivateKey = vaspConfig.getHandshakePrivateKey();
        this.vaspInfo = vaspConfig.getVaspInfo();

        messageService.addTopicListener(
                vaspConfig.getVaspCode().toTopic(),
                EncryptionType.ASSYMETRIC,
                handshakePrivateKey,
                this::onReceiveMessage);
    }

    VaspCode vaspCode() {
        return vaspInfo.getVaspCode();
    }

    @Override
    public OriginatorSession createOriginatorSession(@NonNull final TransferInfo transferInfo) {
        return new OriginatorSessionImpl(this, transferInfo);
    }

    private void onReceiveMessage(@NonNull final TopicEvent<VaspMessage> event) {
        val message = event.getPayload();

        // At the level of the VASP instance we process only session requests
        if (message instanceof SessionRequest) {
            val beneficiarySession = new BeneficiarySessionImpl(this, (SessionRequest) message);
            beneficiarySession.onReceiveMessage(event);
        }
    }

    @Override
    public Optional<BeneficiarySession> waitForBeneficiarySession(
            @NonNull final String sessionId,
            final long msTimeout) {

        sessionsLock.lock();
        try {
            if (beneficiarySessions.containsKey(sessionId)) {
                return Optional.of(beneficiarySessions.get(sessionId));
            } else {
                return newBeneficiarySession.await(msTimeout, TimeUnit.MILLISECONDS)
                        ? getBeneficiarySession(sessionId)
                        : Optional.empty();
            }
        } catch (InterruptedException ex) {
            return Optional.empty();
        } finally {
            sessionsLock.unlock();
        }
    }

    @Override
    public boolean waitForNoActiveSessions(final long msTimeout) {
        sessionsLock.lock();
        try {
            return originatorSessions.isEmpty() && beneficiarySessions.isEmpty() ||
                    noActiveSessions.await(msTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            return false;
        } finally {
            sessionsLock.unlock();
        }
    }

    void addOriginatorSession(@NonNull final OriginatorSessionImpl originatorSession) {
        sessionsLock.lock();
        try {
            originatorSessions.put(originatorSession.sessionId(), originatorSession);
        } finally {
            sessionsLock.unlock();
        }
    }

    void addBenefeciarySession(@NonNull final BeneficiarySessionImpl beneficiarySession) {
        sessionsLock.lock();
        try {
            beneficiarySessions.put(beneficiarySession.sessionId(), beneficiarySession);
            newBeneficiarySession.signalAll();
        } finally {
            sessionsLock.unlock();
        }
    }

    void removeOriginatorSession(@NonNull final OriginatorSessionImpl session) {
        sessionsLock.lock();
        try {
            originatorSessions.remove(session.sessionId());
            if (originatorSessions.isEmpty() && beneficiarySessions.isEmpty()) {
                noActiveSessions.signalAll();
            }
        } finally {
            sessionsLock.unlock();
        }
    }

    void removeBenefeciarySession(@NonNull final BeneficiarySessionImpl session) {
        sessionsLock.lock();
        try {
            beneficiarySessions.remove(session.sessionId());
            if (originatorSessions.isEmpty() && beneficiarySessions.isEmpty()) {
                noActiveSessions.signalAll();
            }
        } finally {
            sessionsLock.unlock();
        }
    }

    @Override
    public Optional<OriginatorSession> getOriginatorSession(@NonNull final String sessionId) {
        val result = originatorSessions.get(sessionId);
        return result == null ? Optional.empty() : Optional.of(result);
    }

    @Override
    public Optional<BeneficiarySession> getBeneficiarySession(@NonNull final String sessionId) {
        val result = beneficiarySessions.get(sessionId);
        return result == null ? Optional.empty() : Optional.of(result);
    }

    @Override
    public List<Session> allSessions() {
        sessionsLock.lock();
        try {
            val result = new ArrayList<Session>();
            result.addAll(originatorSessions.values());
            result.addAll(beneficiarySessions.values());
            return result;
        } finally {
            sessionsLock.unlock();
        }
    }

    @Override
    public Session restoreSession(@NonNull final SessionState sessionState) {
        return sessionState.getType() == SessionState.Type.ORIGINATOR
                ? restoreOriginatorSession(sessionState)
                : restoreBeneficiarySession(sessionState);
    }

    private Session restoreOriginatorSession(@NonNull final SessionState sessionState) {
        return new OriginatorSessionImpl(this, sessionState);
    }

    private Session restoreBeneficiarySession(@NonNull final SessionState sessionState) {
        return new BeneficiarySessionImpl(this, sessionState);
    }

}
