package org.openvasp.client.service.impl;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.openvasp.client.api.whisper.WhisperApi;
import org.openvasp.client.config.VaspConfig;
import org.openvasp.client.model.VaspMessage;
import org.openvasp.client.service.ConfirmationService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Singleton
@Slf4j
public final class EmptyConfirmationServiceImpl extends AbstractTopicService implements ConfirmationService {

    @Inject
    public EmptyConfirmationServiceImpl(
            final VaspConfig vaspConfig,
            final WhisperApi whisper) {

        super(whisper, "PollingLoopC-" + vaspConfig.getVaspInfo().getVaspCode());
        startPolling();
    }

    @Override
    public void registerForConfirmation(@NonNull final VaspMessage message) {

    }

    @Override
    public void confirmReceipt(@NonNull final VaspMessage message) {

    }

    @Override
    Logger log() {
        return log;
    }

    @Override
    void pollMessages() {

    }

}
