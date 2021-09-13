package org.openvasp.client.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openvasp.client.common.ExceptionHandler;
import org.openvasp.client.config.VaspConfig;
import org.openvasp.client.model.EncryptionType;
import org.openvasp.client.model.SessionRequest;
import org.openvasp.client.model.Topic;
import org.openvasp.client.model.VaspMessage;
import org.openvasp.client.service.ConfirmationService;
import org.openvasp.client.service.SignService;
import org.openvasp.client.service.WhisperService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MessageServiceImplTests {

    @Mock
    ExceptionHandler exceptionHandler;
    @Mock
    WhisperService whisperService;
    @Mock
    SignService signService;
    @Mock
    ConfirmationService confirmationService;

    VaspConfig vaspConfig;
    MessageServiceImpl messageService;

    @BeforeEach
    public void init() {
        vaspConfig = new VaspConfig();
        vaspConfig.setHandshakePrivateKey("0xe7578145d518e5272d660ccfdeceedf2d55b90867f2b7a6e54dc726662aebac2");
        messageService = new MessageServiceImpl(vaspConfig, exceptionHandler, whisperService, signService, confirmationService);
    }

    @Test
    public void topicListenerTest() {
        Topic topic = new Topic("0x12345678");
        String keyId = "keyId";
        long id = messageService.addTopicListener(topic, EncryptionType.SYMMETRIC, keyId, event -> {});

        verify(whisperService).addTopicListener(any(), any(), any(), any());
        messageService.removeTopicListener(topic, id);
        verify(whisperService).removeTopicListener(topic, id);
    }

    @Test
    public void sendTest() {
        Topic topic = new Topic("0x12345678");
        String keyId = "keyId";
        VaspMessage vaspMessage = new SessionRequest();
        messageService.send(topic, EncryptionType.SYMMETRIC, keyId, vaspMessage);
        verify(whisperService).send(any(), any(), any(), any());
    }
}
