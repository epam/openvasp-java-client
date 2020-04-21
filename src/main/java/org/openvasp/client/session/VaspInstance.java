package org.openvasp.client.session;

import org.openvasp.client.common.VaspException;
import org.openvasp.client.model.TransferInfo;
import org.openvasp.client.model.VaspMessage;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public interface VaspInstance {

    void setCustomMessageHandler(BiConsumer<VaspMessage, Session> handler);

    void setCustomErrorHandler(BiConsumer<VaspException, Session> handler);

    OriginatorSession createOriginatorSession(TransferInfo transferInfo);

    Optional<BeneficiarySession> waitForBeneficiarySession(String sessionId, long msTimeout);

    Optional<OriginatorSession> getOriginatorSession(String sessionId);

    Optional<BeneficiarySession> getBeneficiarySession(String sessionId);

    List<Session> allSessions();

    boolean waitForNoActiveSessions(long msTimeout);

}
