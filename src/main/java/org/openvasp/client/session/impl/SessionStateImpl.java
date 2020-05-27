package org.openvasp.client.session.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.openvasp.client.model.Topic;
import org.openvasp.client.session.Session;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public final class SessionStateImpl implements Session.State {

    private String id;
    private Session.Type type;
    private Topic incomingTopic;
    private Topic outgoingTopic;

}
