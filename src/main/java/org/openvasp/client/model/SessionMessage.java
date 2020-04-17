package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.openvasp.client.common.VaspValidationException;
import org.web3j.utils.Numeric;

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
        handshake.validate(this);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Handshake {

        // TODO: pending discussions
        //  https://github.com/LykkeBusiness/openvasp-message-samples/issues/1
        //  https://github.com/OpenVASP/openvasp-specification/issues/7
        public static final int ECDHPK_LENGTH = 130;
        
        @JsonProperty("topica")
        private Topic topicA;

        @JsonProperty("topicb")
        private Topic topicB;

        @JsonProperty("ecdhpk")
        private String sessionPublicKey;

        public void validate(VaspMessage source) {
            if(null!=sessionPublicKey && Numeric.cleanHexPrefix(sessionPublicKey).length()!=ECDHPK_LENGTH)
                throw new VaspValidationException(source,
                        "The field 'ecdhpk' is invalid - must be a hexadecimal string of length %d, but is: %s",
                        ECDHPK_LENGTH,
                        sessionPublicKey);
        }
    }

}
