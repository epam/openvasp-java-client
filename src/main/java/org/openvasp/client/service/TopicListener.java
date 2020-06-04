package org.openvasp.client.service;

import lombok.NonNull;

import java.util.EventListener;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@FunctionalInterface
public interface TopicListener<T> extends EventListener {

    void onTopicEvent(TopicEvent<T> event);

    default <U> TopicListener<U> map(@NonNull final Function<U, Optional<T>> payloadMapper) {
        return (event) -> payloadMapper
                .apply(event.getPayload())
                .ifPresent(payload -> onTopicEvent(new TopicEvent<>(event.getSource(), payload)));
    }

}
