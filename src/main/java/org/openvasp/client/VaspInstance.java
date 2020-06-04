package org.openvasp.client;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.openvasp.client.api.whisper.WhisperApi;
import org.openvasp.client.api.whisper.WhisperSymKeyApi;
import org.openvasp.client.common.ExceptionHandler;
import org.openvasp.client.common.ExceptionHandlerDelegate;
import org.openvasp.client.config.VaspModule;
import org.openvasp.client.model.*;
import org.openvasp.client.service.*;
import org.openvasp.client.session.BeneficiarySession;
import org.openvasp.client.session.OriginatorSession;
import org.openvasp.client.session.Session;
import org.openvasp.client.session.SessionManager;
import org.openvasp.client.session.impl.SessionState;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Slf4j
public final class VaspInstance implements
        AutoCloseable,
        WhisperSymKeyApi,
        ContractService,
        MessageService,
        ConfirmationService,
        SessionManager {

    public static final String VERSION = "0.0.1";

    private final VaspModule module;
    private final Injector injector;

    private final WhisperApi whisperApi;
    private final ContractService contractService;
    private final WhisperService whisperService;
    private final MessageService messageService;
    private final ConfirmationService confirmationService;
    private SessionManager sessionManager;

    public VaspInstance(@NonNull final VaspModule module, boolean autoStartSessionManager) {
        this.module = module;
        this.injector = Guice.createInjector(module);
        this.whisperApi = injector.getInstance(WhisperApi.class);
        this.contractService = injector.getInstance(ContractService.class);
        this.whisperService = injector.getInstance(WhisperService.class);
        this.messageService = injector.getInstance(MessageService.class);
        this.confirmationService = injector.getInstance(ConfirmationService.class);

        if (autoStartSessionManager) {
            this.sessionManager = injector.getInstance(SessionManager.class);
        }
    }

    public VaspInstance(@NonNull final VaspModule module) {
        this(module, true);
    }

    public void startSessionManager() {
        // Start listening for SessionRequest's
        sessionManager = injector.getInstance(SessionManager.class);
    }

    @Override
    @SneakyThrows
    public void close() {
        whisperService.close();
        module.close();
    }

    public void shutdown() {
        whisperService.shutdown();
    }

    public boolean waitForTermination(final long msTimeout) {
        return whisperService.waitForTermination(msTimeout);
    }

    @Override
    public String newSymKey() {
        return whisperApi.newSymKey();
    }

    @Override
    public String generateSymKeyFromPassword(String password) {
        return whisperApi.generateSymKeyFromPassword(password);
    }

    @Override
    public String addSymKey(String key) {
        return whisperApi.addSymKey(key);
    }

    @Override
    public Boolean deleteSymKey(String symKeyId) {
        return whisperApi.deleteSymKey(symKeyId);
    }

    @Override
    public Boolean hasSymKey(String symKeyId) {
        return whisperApi.hasSymKey(symKeyId);
    }

    @Override
    public String getSymKey(String symKeyId) {
        return whisperApi.getSymKey(symKeyId);
    }

    @Override
    public VaspContractInfo getVaspContractInfo(@NonNull final EthAddr vaspSmartContractAddress) {
        return contractService.getVaspContractInfo(vaspSmartContractAddress);
    }

    @Override
    public VaspContractInfo getVaspContractInfo(@NonNull final VaspCode vaspCode) {
        return contractService.getVaspContractInfo(vaspCode);
    }

    @Override
    public void send(
            @NonNull final Topic topic,
            @NonNull final EncryptionType encType,
            @NonNull final String key,
            @NonNull final VaspMessage message) {

        messageService.send(topic, encType, key, message);
    }

    @Override
    public long addTopicListener(
            @NonNull final Topic topic,
            @NonNull final EncryptionType encType,
            @NonNull final String key,
            @NonNull final TopicListener<VaspMessage> listener) {

        return messageService.addTopicListener(topic, encType, key, listener);
    }

    @Override
    public void removeTopicListener(
            @NonNull final Topic topic,
            final long listenerId) {

        messageService.removeTopicListener(topic, listenerId);
    }

    @Override
    public void registerForConfirmation(@NonNull final VaspMessage message) {
        confirmationService.registerForConfirmation(message);
    }

    @Override
    public void setConfirmationHandler(Consumer<VaspMessage> handler) {
        confirmationService.setConfirmationHandler(handler);
    }

    @Override
    public void confirmReceipt(@NonNull final VaspMessage message) {
        confirmationService.confirmReceipt(message);
    }

    @Override
    public void setMessageHandler(final BiConsumer<VaspMessage, Session> handler) {
        sessionManager.setMessageHandler(handler);
    }

    public void setExceptionHandler(final ExceptionHandler handler) {
        val delegateHandler = (ExceptionHandlerDelegate) injector.getInstance(ExceptionHandler.class);
        delegateHandler.setDelegate(handler);
    }

    @Override
    public OriginatorSession createOriginatorSession(@NonNull final TransferInfo transferInfo) {
        return sessionManager.createOriginatorSession(transferInfo);
    }

    @Override
    public Optional<BeneficiarySession> waitForBeneficiarySession(
            @NonNull final String sessionId,
            final long msTimeout) {

        return sessionManager.waitForBeneficiarySession(sessionId, msTimeout);
    }

    @Override
    public Optional<OriginatorSession> getOriginatorSession(@NonNull final String sessionId) {
        return sessionManager.getOriginatorSession(sessionId);
    }

    @Override
    public Optional<BeneficiarySession> getBeneficiarySession(@NonNull final String sessionId) {
        return sessionManager.getBeneficiarySession(sessionId);
    }

    @Override
    public List<Session> allSessions() {
        return sessionManager.allSessions();
    }

    @Override
    public boolean waitForNoActiveSessions(final long msTimeout) {
        return sessionManager.waitForNoActiveSessions(msTimeout);
    }

    @Override
    public Session restoreSession(SessionState sessionState) {
        return sessionManager.restoreSession(sessionState);
    }

}
