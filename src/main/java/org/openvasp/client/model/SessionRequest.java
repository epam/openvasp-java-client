package org.openvasp.client.model;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public final class SessionRequest extends SessionMessage {

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
