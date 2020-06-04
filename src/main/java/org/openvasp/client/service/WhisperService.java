package org.openvasp.client.service;

import org.openvasp.client.api.whisper.model.ShhMessage;
import org.openvasp.client.model.EncryptionType;
import org.openvasp.client.model.Topic;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public interface WhisperService extends AutoCloseable {

    void shutdown();

    boolean waitForTermination(
            long msTimeout);

    void send(
            Topic topic,
            EncryptionType encType,
            String key,
            String payload);

    long addTopicListener(
            Topic topic,
            EncryptionType encType,
            String key,
            TopicListener<ShhMessage> listener);

    void removeTopicListener(
            Topic topic,
            long listenerId);

}
