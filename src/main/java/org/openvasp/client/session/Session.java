package org.openvasp.client.session;

import org.openvasp.client.common.VaspException;
import org.openvasp.client.model.TransferInfo;
import org.openvasp.client.model.VaspCode;
import org.openvasp.client.model.VaspInfo;
import org.openvasp.client.model.VaspMessage;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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

    List<VaspException> errors();

    BiConsumer<VaspMessage, Session> getCustomMessageHandler();

    void setCustomMessageHandler(BiConsumer<VaspMessage, Session> handler);

    BiConsumer<VaspException, Session> getCustomErrorHandler();

    void setCustomErrorHandler(BiConsumer<VaspException, Session> handler);

    Optional<VaspMessage> waitForNewMessage(long timeout, TimeUnit unit);

    void remove();

}
