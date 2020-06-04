package org.openvasp.client.service;

import lombok.Getter;
import lombok.NonNull;
import org.openvasp.client.model.Topic;

import java.util.EventObject;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public final class TopicEvent<T> extends EventObject {

    @Getter
    private final T payload;

    public TopicEvent(@NonNull final Topic topic, @NonNull final T payload) {
        super(topic);
        this.payload = payload;
    }

    @Override
    public Topic getSource() {
        return (Topic) super.getSource();
    }

}
