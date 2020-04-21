package org.openvasp.client;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openvasp.client.api.whisper.WhisperApi;
import org.openvasp.client.api.whisper.WhisperSymKeyApi;
import org.openvasp.client.common.VaspException;
import org.openvasp.client.config.VaspConfig;
import org.openvasp.client.config.VaspModule;
import org.openvasp.client.model.*;
import org.openvasp.client.service.ContractService;
import org.openvasp.client.service.MessageService;
import org.openvasp.client.service.TopicListener;
import org.openvasp.client.session.BeneficiarySession;
import org.openvasp.client.session.OriginatorSession;
import org.openvasp.client.session.Session;
import org.openvasp.client.session.VaspInstance;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Slf4j
public final class VaspClient implements
        AutoCloseable,
        WhisperSymKeyApi,
        ContractService,
        MessageService,
        VaspInstance {

    public static final String VERSION = "0.0.1";

    private final VaspModule module;
    private final Injector injector;

    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final WhisperApi whisperApi = injector.getInstance(WhisperApi.class);

    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final ContractService contractService = injector.getInstance(ContractService.class);

    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final MessageService messageService = injector.getInstance(MessageService.class);

    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final VaspInstance vaspInstance = injector.getInstance(VaspInstance.class);

    public VaspClient(@NonNull final VaspModule module) {
        this.module = module;
        this.injector = Guice.createInjector(module);
        // Create and start MessageService
        getMessageService();
    }

    public VaspClient(@NonNull final VaspConfig vaspConfig) {
        this(new VaspModule(vaspConfig));
    }

    public void logVersion() {
        log.info("OpenVASP client, v{}, VASP code: {}", VERSION, module.getVaspCode());
    }

    @Override
    @SneakyThrows
    public void close() {
        getMessageService().close();
        module.close();
    }

    @Override
    public void shutdown() {
        getMessageService().shutdown();
    }

    @Override
    public boolean waitForTermination(final long msTimeout) {
        return getMessageService().waitForTermination(msTimeout);
    }

    @Override
    public String newSymKey() {
        return getWhisperApi().newSymKey();
    }

    @Override
    public String generateSymKeyFromPassword(String password) {
        return getWhisperApi().generateSymKeyFromPassword(password);
    }

    @Override
    public String addSymKey(String key) {
        return getWhisperApi().addSymKey(key);
    }

    @Override
    public Boolean deleteSymKey(String symKeyId) {
        return getWhisperApi().deleteSymKey(symKeyId);
    }

    @Override
    public Boolean hasSymKey(String symKeyId) {
        return getWhisperApi().hasSymKey(symKeyId);
    }

    @Override
    public String getSymKey(String symKeyId) {
        return getWhisperApi().getSymKey(symKeyId);
    }

    @Override
    public VaspContractInfo getVaspContractInfo(@NonNull final String vaspSmartContractAddress) {
        return getContractService().getVaspContractInfo(vaspSmartContractAddress);
    }

    @Override
    public VaspContractInfo getVaspContractInfo(@NonNull final VaspCode vaspCode) {
        return getContractService().getVaspContractInfo(vaspCode);
    }

    @Override
    public void send(
            @NonNull final Topic topic,
            @NonNull final EncryptionType encType,
            @NonNull final String key,
            @NonNull final VaspMessage message) {

        getMessageService().send(topic, encType, key, message);
    }

    @Override
    public void addTopicListener(
            @NonNull final Topic topic,
            @NonNull final EncryptionType encType,
            @NonNull final String key,
            @NonNull final TopicListener listener) {

        getMessageService().addTopicListener(topic, encType, key, listener);
    }

    @Override
    public void removeTopicListener(@NonNull final Topic topic, @NonNull final TopicListener listener) {
        getMessageService().removeTopicListener(topic, listener);
    }

    @Override
    public void removeTopicListeners(@NonNull final Topic topic) {
        getMessageService().removeTopicListeners(topic);
    }

    @Override
    public void setCustomMessageHandler(final BiConsumer<VaspMessage, Session> handler) {
        getVaspInstance().setCustomMessageHandler(handler);
    }

    @Override
    public void setCustomErrorHandler(final BiConsumer<VaspException, Session> handler) {
        getVaspInstance().setCustomErrorHandler(handler);
    }

    @Override
    public OriginatorSession createOriginatorSession(@NonNull final TransferInfo transferInfo) {
        return getVaspInstance().createOriginatorSession(transferInfo);
    }

    @Override
    public Optional<BeneficiarySession> waitForBeneficiarySession(
            @NonNull final String sessionId,
            final long msTimeout) {

        return getVaspInstance().waitForBeneficiarySession(sessionId, msTimeout);
    }

    @Override
    public Optional<OriginatorSession> getOriginatorSession(@NonNull final String sessionId) {
        return getVaspInstance().getOriginatorSession(sessionId);
    }

    @Override
    public Optional<BeneficiarySession> getBeneficiarySession(@NonNull final String sessionId) {
        return getVaspInstance().getBeneficiarySession(sessionId);
    }

    @Override
    public List<Session> allSessions() {
        return getVaspInstance().allSessions();
    }

    @Override
    public boolean waitForNoActiveSessions(final long msTimeout) {
        return getVaspInstance().waitForNoActiveSessions(msTimeout);
    }

}
