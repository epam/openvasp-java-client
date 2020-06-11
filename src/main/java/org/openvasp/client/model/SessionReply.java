package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
public final class SessionReply extends SessionMessage {

    @JsonProperty("handshake")
    private Handshake handshake = new Handshake();

    {
        getHeader().setMessageType(TypeDescriptor.SESSION_REPLY);
    }

    @Override
    public void validate() {
        super.validate();
        validateNotNull(handshake.getTopicB(), "handshake.topicb");
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Handshake {

        @JsonProperty("topicb")
        private Topic topicB;

    }

}
