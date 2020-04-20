package org.openvasp.client.session;

import lombok.val;
import org.openvasp.client.common.VaspException;
import org.openvasp.client.model.TransferInfo;
import org.openvasp.client.model.VaspCode;
import org.openvasp.client.model.VaspInfo;
import org.openvasp.client.model.VaspMessage;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public interface Session {

    String sessionId();

    VaspCode vaspCode();

    VaspCode peerVaspCode();

    VaspInfo peerVaspInfo();

    TransferInfo transferInfo();

    void sendMessage(VaspMessage vaspMessage);

    List<VaspMessage> incomingMessages();

    default VaspMessage lastMessage() {
        val incomingMessages = incomingMessages();
        return incomingMessages.get(incomingMessages.size() - 1);
    }

    @SuppressWarnings("unchecked")
    default <T extends VaspMessage> T lastMessage(Class<T> messageClass) {
        return (T) lastMessage();
    }

    List<VaspException> errors();

    BiConsumer<VaspMessage, Session> getCustomMessageHandler();

    void setCustomMessageHandler(BiConsumer<VaspMessage, Session> handler);

    BiConsumer<VaspException, Session> getCustomErrorHandler();

    void setCustomErrorHandler(BiConsumer<VaspException, Session> handler);

    <T extends VaspMessage> Optional<T> waitForNewMessage(Class<T> messageClass, long timeout);

    default Optional<VaspMessage> waitForNewMessage(long timeout) {
        return waitForNewMessage(VaspMessage.class, timeout);
    }

    void remove();

    Object getAttr(String key);

    Object putAttr(String key, Object value);

    Object removeAttr(String key);

}
