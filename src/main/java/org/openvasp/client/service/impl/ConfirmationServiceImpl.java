package org.openvasp.client.service.impl;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openvasp.client.common.Tuple2;
import org.openvasp.client.common.VaspValidationException;
import org.openvasp.client.config.VaspConfig;
import org.openvasp.client.model.EncryptionType;
import org.openvasp.client.model.VaspContractInfo;
import org.openvasp.client.model.VaspMessage;
import org.openvasp.client.service.ConfirmationService;
import org.openvasp.client.service.ContractService;
import org.openvasp.client.service.VaspIdentityService;
import org.openvasp.client.service.WhisperService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Singleton
@Slf4j
public final class ConfirmationServiceImpl implements ConfirmationService {

    private final VaspConfig vaspConfig;
    private final WhisperService whisperService;
    private final ContractService contractService;
    private final VaspIdentityService vaspIdentityService;

    @Setter
    private Consumer<VaspMessage> confirmationHandler;

    private final Map<String, Tuple2<Long, VaspMessage>> waitingForConfirmation = new ConcurrentHashMap<>();

    @Inject
    public ConfirmationServiceImpl(
            final VaspConfig vaspConfig,
            final WhisperService whisperService,
            final ContractService contractService,
            final VaspIdentityService vaspIdentityService) {

        this.vaspConfig = vaspConfig;
        this.whisperService = whisperService;
        this.contractService = contractService;
        this.vaspIdentityService = vaspIdentityService;

        log.info("Confirmation Service created");
    }

    @Override
    public void registerForConfirmation(@NonNull final VaspMessage message) {
        if (!isConfirmationEnabled()) {
            return;
        }

        var messageId = message.getHeader().getMessageId();
        var confirmationTopic = message.getConfirmationTopic();

        var listenerId = whisperService.addTopicListener(
                confirmationTopic,
                EncryptionType.ASSYMETRIC,
                getConfirmationPrivateKey(),
                whisperMessage -> {
                    var entry = waitingForConfirmation.get(messageId);
                    if (entry != null) {
                        waitingForConfirmation.remove(messageId);
                        whisperService.removeTopicListener(confirmationTopic, entry._1);
                        if (confirmationHandler != null) {
                            confirmationHandler.accept(message);
                        }
                    }
                });

        waitingForConfirmation.put(messageId, Tuple2.of(listenerId, message));
    }

    @Override
    public void confirmReceipt(@NonNull final VaspMessage message) {
        if (isConfirmationEnabled()) {
            var confirmationTopic = message.getConfirmationTopic();
            whisperService.send(
                    confirmationTopic,
                    EncryptionType.ASSYMETRIC,
                    getConfirmationPublicKey(message),
                    "0x10");
        }
    }

    private String getConfirmationPrivateKey() {
        return vaspConfig.getHandshakePrivateKey();
    }

    private String getConfirmationPublicKey(@NonNull final VaspMessage message) {
        return vaspIdentityService.resolveSenderVaspId(message)
                .map(contractService::getVaspContractInfo)
                .map(VaspContractInfo::getHandshakeKey)
                .orElseThrow(() -> new VaspValidationException(message, "Sender's VASP ID cannot be resolved"));
    }

    private boolean isConfirmationEnabled() {
        return Boolean.TRUE.equals(vaspConfig.getAcknowledgmentEnabled());
    }

}
