package org.openvasp.client.session;

import org.openvasp.client.model.EthAddr;
import org.openvasp.client.model.TransferInfo;
import org.openvasp.client.model.VaspMessage;
import org.openvasp.client.session.impl.SessionState;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public interface SessionManager {

    void setMessageHandler(BiConsumer<VaspMessage, Session> handler);

    OriginatorSession createOriginatorSession(TransferInfo transferInfo);

    Optional<BeneficiarySession> waitForBeneficiarySession(String sessionId, long msTimeout);

    Optional<OriginatorSession> getOriginatorSession(String sessionId);

    Optional<BeneficiarySession> getBeneficiarySession(String sessionId);

    List<Session> allSessions();

    boolean waitForNoActiveSessions(long msTimeout);

    Session restoreSession(SessionState sessionState);

    Optional<EthAddr> resolveSenderVaspId(VaspMessage message);

}
