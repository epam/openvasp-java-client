package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public final class SessionRequest extends SessionMessage {

    @Getter
    @JsonIgnore
    private String sharedSecret;

    {
        getHeader().setMessageType(TypeDescriptor.SESSION_REQUEST);
    }

    @Override
    public void validate() {
        super.validate();
        validateNotNull(getHandshake().getTopicA(), "handshake.topica");
        validateNotNull(getHandshake().getSessionPublicKey(), "handshake.ecdhpk");
    }

}
