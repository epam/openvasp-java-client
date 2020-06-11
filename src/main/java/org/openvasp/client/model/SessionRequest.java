package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.openvasp.client.common.VaspUtils;
import org.openvasp.client.common.VaspValidationException;
import org.web3j.utils.Numeric;

import static org.openvasp.client.common.Constants.ECDHPK_LENGTH;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
public final class SessionRequest extends SessionMessage {

    @JsonProperty("handshake")
    private Handshake handshake = new Handshake();

    {
        getHeader().setMessageType(TypeDescriptor.SESSION_REQUEST);
    }

    @Override
    public void validate() {
        super.validate();
        validateNotNull(handshake.getTopicA(), "handshake.topica");
        validateNotNull(handshake.getSessionPublicKey(), "handshake.ecdhpk");

        val sessionPublicKey = Numeric.cleanHexPrefix(handshake.getSessionPublicKey());
        if (!VaspUtils.isValidHex(sessionPublicKey) ||
                sessionPublicKey.length() != ECDHPK_LENGTH && sessionPublicKey.length() != ECDHPK_LENGTH - 2) {
            throw new VaspValidationException(this,
                    "The field 'ecdhpk' is invalid - must be a hexadecimal string of length %d, but is: %s",
                    ECDHPK_LENGTH,
                    sessionPublicKey);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Handshake {

        @JsonProperty("topica")
        private Topic topicA;

        @JsonProperty("ecdhpk")
        private String sessionPublicKey;

    }


}


