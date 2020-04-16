package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
public abstract class SessionMessage extends VaspMessage {

    @JsonProperty("handshake")
    private Handshake handshake;

    @Override
    public void validate() {
        super.validate();
        validateNotNull(handshake, "handshake");
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Handshake {

        @JsonProperty("topica")
        private Topic topicA;

        @JsonProperty("topicb")
        private Topic topicB;

        @JsonProperty("ecdhpk")
        private String sessionPublicKey;

    }

}
