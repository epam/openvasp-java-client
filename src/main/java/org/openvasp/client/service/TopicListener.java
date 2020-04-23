package org.openvasp.client.service;

import lombok.NonNull;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EventListener;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@FunctionalInterface
public interface TopicListener extends EventListener {

    void onReceiveMessage(TopicEvent event);

    default void onError(@NonNull final TopicErrorEvent event) {
        val log = getTopicListenerLogger();
        log.error("Error in TopicListener", event.getCause());
    }

    default Logger getTopicListenerLogger() {
        return LoggerFactory.getLogger(getClass());
    }

}