package org.openvasp.client.session;

import lombok.NonNull;
import org.openvasp.client.common.VaspException;
import org.openvasp.client.model.TransferInfo;
import org.openvasp.client.model.VaspMessage;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public interface VaspInstance {

    void setCustomMessageHandler(BiConsumer<VaspMessage, Session> handler);

    void setCustomErrorHandler(BiConsumer<VaspException, Session> handler);

    OriginatorSession createOriginatorSession(TransferInfo transferInfo);

    Optional<BeneficiarySession> waitForBeneficiarySession(String sessionId, long timeout, TimeUnit unit);

    Optional<OriginatorSession> getOriginatorSession(String sessionId);

    Optional<BeneficiarySession> getBeneficiarySession(String sessionId);

    boolean waitForNoActiveSessions(long timeout, TimeUnit unit);

}
