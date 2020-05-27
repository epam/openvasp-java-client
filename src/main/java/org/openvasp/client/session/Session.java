package org.openvasp.client.session;

import lombok.NonNull;
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

    default VaspMessage lastIncomingMessage() {
        val incomingMessages = incomingMessages();
        return incomingMessages.get(incomingMessages.size() - 1);
    }

    @SuppressWarnings("unchecked")
    default <T extends VaspMessage> T lastIncomingMessage(Class<T> messageClass) {
        return (T) lastIncomingMessage();
    }

    default Optional<VaspMessage> incomingMessageById(@NonNull final String messageId) {
        return incomingMessages()
                .stream()
                .filter(message -> messageId.equals(message.getHeader().getMessageId()))
                .findFirst();
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

    /*
     * Management of sessions' persistence. The State interface defines all necessary date that
     * have to be stored in order to re-create the session later.
     */

    enum Type {
        ORIGINATOR,
        BENEFICIARY
    }

    interface State {

        Type getType();

        String getId();

    }

    State getState();

}
