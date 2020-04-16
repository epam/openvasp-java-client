package org.openvasp.client.service;

import lombok.Getter;
import lombok.NonNull;
import org.openvasp.client.model.Topic;
import org.openvasp.client.model.VaspMessage;

import java.util.EventObject;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public final class TopicEvent extends EventObject {

    @Getter
    private final VaspMessage message;

    public TopicEvent(@NonNull final Topic topic, @NonNull final VaspMessage message) {
        super(topic);
        this.message = message;
    }

    @Override
    public Topic getSource() {
        return (Topic) super.getSource();
    }

}
