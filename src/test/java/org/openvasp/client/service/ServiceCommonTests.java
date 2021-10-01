package org.openvasp.client.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openvasp.client.api.whisper.WhisperApiError;
import org.openvasp.client.api.whisper.WhisperIOException;
import org.openvasp.client.model.Topic;
import org.web3j.protocol.core.Response;

import java.io.IOException;

public class ServiceCommonTests {

    @Test
    public void topicEventGetSourceTest() {
        Topic topic = new Topic("0x527aeb21");
        TopicEvent<String> topicEvent = new TopicEvent<>(topic, "payload");
        Assertions.assertEquals(topic, topicEvent.getSource());
    }

    @Test
    public void whisperApiErrorMessageTest() {
        String errorMessage = "Whisper API error";
        WhisperApiError whisperApiError = new WhisperApiError(new Response.Error(), errorMessage);
        Assertions.assertEquals(errorMessage, whisperApiError.getRawResponse());
    }

    @Test
    public void whisperIOExceptionMessageTest() {
        String errorMessage = "Whisper IO exception";
        WhisperIOException exception = new WhisperIOException(new IOException(errorMessage));
        Assertions.assertEquals("java.io.IOException: " + errorMessage, exception.getMessage());
    }
}
