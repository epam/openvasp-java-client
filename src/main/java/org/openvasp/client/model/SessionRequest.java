package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.openvasp.client.common.VaspUtils;
import org.openvasp.client.common.VaspValidationException;
import org.web3j.utils.Numeric;

import static com.google.common.base.Preconditions.checkState;
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

    @JsonIgnore
    public String getSessionPublicKey() {
        checkState(handshake != null, "handshake must not be null");
        checkState(StringUtils.isNotEmpty(handshake.sessionPublicKey), "handshake.ecdhpk must not be empty");

        val sessionPublicKey = Numeric.cleanHexPrefix(handshake.sessionPublicKey);

        if (sessionPublicKey.length() == ECDHPK_LENGTH) {
            return handshake.sessionPublicKey;
        }

        if (sessionPublicKey.length() == ECDHPK_LENGTH - 2) {
            return "0x04" + sessionPublicKey;
        }

        throw new VaspValidationException(this,
                "The field 'ecdhpk' is invalid - must be a hexadecimal string of length %d, but is: %s",
                ECDHPK_LENGTH,
                sessionPublicKey);
    }


    @Override
    public void validate() {
        super.validate();
        validateNotNull(handshake.getTopicA(), "handshake.topica");
        validateNotNull(handshake.getSessionPublicKey(), "handshake.ecdhpk");

        val sessionPublicKey = Numeric.cleanHexPrefix(handshake.sessionPublicKey);
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


