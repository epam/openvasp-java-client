package org.openvasp.client.model;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public final class SessionReply extends SessionMessage {

    {
        getHeader().setMessageType(TypeDescriptor.SESSION_REPLY);
    }

    @Override
    public void validate() {
        super.validate();
        validateNotNull(getHandshake().getTopicB(), "handshake.topicb");
        validateNotNull(getHeader().getResponseCode(), "msg.code");
    }

}
