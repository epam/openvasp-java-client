package org.openvasp.client.service.impl;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.openvasp.client.api.whisper.model.ShhMessage;
import org.openvasp.client.common.ExceptionHandler;
import org.openvasp.client.common.VaspValidationException;
import org.openvasp.client.config.VaspConfig;
import org.openvasp.client.model.EncryptionType;
import org.openvasp.client.model.Topic;
import org.openvasp.client.model.VaspInfo;
import org.openvasp.client.model.VaspMessage;
import org.openvasp.client.service.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Singleton
@Slf4j
public final class MessageServiceImpl implements MessageService {

    private final WhisperService whisperService;
    private final SignService signService;
    private final ConfirmationService confirmationService;
    private final ExceptionHandler exceptionHandler;

    private final VaspInfo senderVaspInfo;
    private final String senderSigningPrivateKey;

    @Inject
    public MessageServiceImpl(
            final VaspConfig vaspConfig,
            final ExceptionHandler exceptionHandler,
            final WhisperService whisperService,
            final SignService signService,
            final ConfirmationService confirmationService) {

        this.whisperService = whisperService;
        this.signService = signService;
        this.confirmationService = confirmationService;

        this.senderVaspInfo = vaspConfig.getVaspInfo();
        this.senderSigningPrivateKey = vaspConfig.getSigningPrivateKey();
        this.exceptionHandler = exceptionHandler;
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
        whisperService.send(topic, encType, key, signService.makeSignedPayload(message, senderSigningPrivateKey));
    }

    @Override
    public long addTopicListener(
            @NonNull final Topic topic,
            @NonNull final EncryptionType encType,
            @NonNull final String key,
            @NonNull final TopicListener<VaspMessage> listener) {

        return whisperService.addTopicListener(
                topic,
                encType,
                key,
                listener.map(this::transformWhisperToVaspMessage));
    }

    @Override
    public void removeTopicListener(
            @NonNull final Topic topic,
            long listenerId) {

        whisperService.removeTopicListener(topic, listenerId);
    }

    private Optional<VaspMessage> transformWhisperToVaspMessage(@NonNull final ShhMessage whisperMessage) {
        try {
            val vaspMessage = signService.extractSignedMessage(whisperMessage.getPayload());
            confirmationService.confirmReceipt(vaspMessage);
            return Optional.of(vaspMessage);
        } catch (VaspValidationException ex) {
            log.error("Error of processing an incoming message at the topic " + whisperMessage.getTopic(), ex);
            exceptionHandler.processException(ex);
            return Optional.empty();
        }
    }

}
