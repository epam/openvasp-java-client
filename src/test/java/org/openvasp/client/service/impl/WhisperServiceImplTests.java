package org.openvasp.client.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openvasp.client.api.whisper.WhisperApi;
import org.openvasp.client.api.whisper.model.ShhMessage;
import org.openvasp.client.common.ExceptionHandler;
import org.openvasp.client.config.VaspConfig;
import org.openvasp.client.model.*;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WhisperServiceImplTests {

    @Mock
    WhisperApi whisperApi;
    @Mock
    ExceptionHandler exceptionHandler;

    VaspConfig vaspConfig;
    WhisperServiceImpl whisperService;

    @BeforeEach
    public void init() {
        EthAddr ethAddr = new EthAddr("0x6befaf0656b953b188a0ee3bf3db03d07dface61");
        VaspInfo vaspInfo = new VaspInfo();
        vaspInfo.setVaspId(ethAddr);

        vaspConfig = new VaspConfig();
        vaspConfig.setVaspInfo(vaspInfo);
        vaspConfig.setVaspCode(new VaspCode("7dface61"));
        vaspConfig.setHandshakePrivateKey("0xe7578145d518e5272d660ccfdeceedf2d55b90867f2b7a6e54dc726662aebac2");

        whisperService = new WhisperServiceImpl(vaspConfig, exceptionHandler, whisperApi);
    }

    @Test
    public void waitForTerminationTest() {
        Assertions.assertFalse(whisperService.waitForTermination(1L));
    }

    @Test
    public void sendTest() {
        whisperService.send(new Topic("0x12345678"), EncryptionType.ASSYMETRIC, "key", "payload");
        whisperService.send(new Topic("0x12345678"), EncryptionType.SYMMETRIC, "key", "payload");
        verify(whisperApi, times(2)).post(any());
    }

    @Test
    public void removeTopicListenerTest() {
        Topic topic = new Topic("0x12345678");
        String keyId = "keyId";
        String filterId = "filterId";

        when(whisperApi.addSymKey(any())).thenReturn(keyId);
        when(whisperApi.newMessageFilter(any())).thenReturn(filterId);
        when(whisperApi.addPrivateKey(any())).thenReturn(keyId);

        WhisperServiceImpl.TopicListenerRecord topicListenerRecord = whisperService.new TopicListenerRecord(topic, EncryptionType.SYMMETRIC, keyId);
        long listenerId = whisperService.addTopicListener(topic, EncryptionType.SYMMETRIC, keyId, topicListenerRecord);
        whisperService.removeTopicListener(topic, listenerId);

        topicListenerRecord = whisperService.new TopicListenerRecord(topic, EncryptionType.ASSYMETRIC, keyId);
        listenerId = whisperService.addTopicListener(topic, EncryptionType.ASSYMETRIC, keyId, topicListenerRecord);
        whisperService.removeTopicListener(topic, listenerId);

        Assertions.assertTrue(topicListenerRecord.getTopicListeners().isEmpty());
    }

    @Test
    public void processIncomingMessagesTest() throws InterruptedException {
        Topic topic = new Topic("0x12345678");
        String key = "key";
        String filterId = "filterId";
        ArrayList<ShhMessage> messages = new ArrayList<>();
        ShhMessage shhMessage = new ShhMessage();
        shhMessage.setTopic(topic.getData());
        messages.add(shhMessage);

        when(whisperApi.newMessageFilter(any())).thenReturn(filterId);
        when(whisperApi.getFilterMessages(any())).thenReturn(messages);

        WhisperServiceImpl.TopicListenerRecord topicListenerRecord = whisperService.new TopicListenerRecord(topic, EncryptionType.SYMMETRIC, key);
        WhisperServiceImpl.TopicListenerRecord spy = spy(topicListenerRecord);

        whisperService.addTopicListener(topic, EncryptionType.SYMMETRIC, key, spy);
        Thread.sleep(2000);
        verify(spy, times(2)).onTopicEvent(any());
    }

    @Test
    public void closeAndShutdownTest() {
        whisperService.close();
        whisperService.shutdown();
    }
}
