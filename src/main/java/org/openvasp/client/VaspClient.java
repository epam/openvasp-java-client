package org.openvasp.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openvasp.client.api.whisper.WhisperApi;
import org.openvasp.client.common.VaspException;
import org.openvasp.client.config.VaspConfig;
import org.openvasp.client.config.VaspModule;
import org.openvasp.client.model.EncryptionType;
import org.openvasp.client.model.Topic;
import org.openvasp.client.model.TransferInfo;
import org.openvasp.client.model.VaspMessage;
import org.openvasp.client.service.*;
import org.openvasp.client.session.BeneficiarySession;
import org.openvasp.client.session.OriginatorSession;
import org.openvasp.client.session.Session;
import org.openvasp.client.session.VaspInstance;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Slf4j
public final class VaspClient implements AutoCloseable, MessageService, VaspInstance {

    public static final String VERSION = "0.0.1";

    private final VaspModule module;
    private final Injector injector;

    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final MessageService messageService = injector.getInstance(MessageService.class);

    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final VaspInstance vaspInstance = injector.getInstance(VaspInstance.class);

    public VaspClient(@NonNull final VaspModule module) {
        this.module = module;
        this.injector = Guice.createInjector(module);
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
    public boolean waitForNoActiveSessions(final long msTimeout) {
        return getVaspInstance().waitForNoActiveSessions(msTimeout);
    }

    @VisibleForTesting
    Injector getInjector() {
        return injector;
    }

    @VisibleForTesting
    WhisperApi getWhisperApi() {
        return injector.getInstance(WhisperApi.class);
    }

    @VisibleForTesting
    SignService getSignService() {
        return injector.getInstance(SignService.class);
    }

    @VisibleForTesting
    EnsService getEnsService() {
        return injector.getInstance(EnsService.class);
    }

    @VisibleForTesting
    ContractService getContractService() {
        return injector.getInstance(ContractService.class);
    }

}
