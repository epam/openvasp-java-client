package org.openvasp.client.service;

import java.util.EventListener;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@FunctionalInterface
public interface TopicListener extends EventListener {

    void onReceiveMessage(TopicEvent event);

    default void onError(TopicErrorEvent event) {
        throw event.getCause();
    }

}