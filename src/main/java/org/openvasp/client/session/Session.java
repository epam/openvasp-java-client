package org.openvasp.client.session;

import org.openvasp.client.model.TransferInfo;
import org.openvasp.client.model.VaspCode;
import org.openvasp.client.model.VaspInfo;
import org.openvasp.client.model.VaspMessage;
import org.openvasp.client.session.impl.SessionState;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public interface Session {

    String sessionId();

    VaspInfo vaspInfo();

    VaspInfo peerVaspInfo();

    TransferInfo transferInfo();

    void sendMessage(VaspMessage vaspMessage);

    Optional<VaspMessage> takeIncomingMessage(long timeout);

    void setMessageHandler(BiConsumer<VaspMessage, Session> handler);

    void remove();

    /*
     * Management of sessions' persistence. The State interface defines all necessary date that
     * have to be stored in order to re-create the session later.
     */

    SessionState getState();

}
