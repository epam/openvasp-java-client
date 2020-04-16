package org.openvasp.client.service;

import lombok.Getter;
import lombok.NonNull;
import org.openvasp.client.common.VaspException;
import org.openvasp.client.model.Topic;

import java.util.EventObject;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public final class TopicErrorEvent extends EventObject {

    @Getter
    private final VaspException cause;

    public TopicErrorEvent(@NonNull final Topic topic, @NonNull final VaspException cause) {
        super(topic);
        this.cause = cause;
    }

    @Override
    public Topic getSource() {
        return (Topic) super.getSource();
    }

}
