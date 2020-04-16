package org.openvasp.client.service;

import org.openvasp.client.model.EncryptionType;
import org.openvasp.client.model.Topic;
import org.openvasp.client.model.VaspMessage;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public interface MessageService extends AutoCloseable {

    void send(
            Topic topic,
            EncryptionType encType,
            String key,
            VaspMessage message);

    void addTopicListener(
            Topic topic,
            EncryptionType encType,
            String key,
            TopicListener listener);

    void removeTopicListener(
            Topic topic,
            TopicListener listener);

    void removeTopicListeners(Topic topic);

    void shutdown();

    boolean waitForTermination(long msTimeout);

    @Override
    void close();

}
