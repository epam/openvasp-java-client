package org.openvasp.client.session.impl;

import lombok.NonNull;
import lombok.val;
import org.openvasp.client.common.VaspUtils;
import org.openvasp.client.crypto.ECDHKeyPair;
import org.openvasp.client.model.*;
import org.openvasp.client.service.TopicEvent;
import org.openvasp.client.session.OriginatorSession;

import static org.openvasp.client.model.VaspResponseCode.OK;

/**
 * @author Olexandr_Bilovol@epam.com
 */
final class OriginatorSessionImpl extends AbstractSession implements OriginatorSession {

    OriginatorSessionImpl(
            @NonNull final SessionManagerImpl owner,
            @NonNull final TransferInfo transferInfo) {

        super(owner, VaspUtils.newSessionId());

        // 6 items to init, the same as in BeneficiarySessionImpl
        this.peerVaspInfo = null;
        this.transferInfo = transferInfo;
        this.topicA = Topic.newRandom();
        this.topicB = null;
        this.sessionKeyPair = ECDHKeyPair.generateKeyPair();
        val contract = contractService().getVaspContractInfo(peerVaspCode());
        this.sharedSecret = sessionKeyPair.generateSharedSecretHex(contract.getHandshakeKey());
    }

    @Override
    public VaspCode peerVaspCode() {
        return transferInfo.getBeneficiary().getVaan().getVaspCode();
    }

    @Override
    String sharedSecret() {
        return sharedSecret;
    }

    @Override
    Topic incomingMessageTopic() {
        return topicA;
    }

    @Override
    Topic outgoingMessageTopic() {
        return topicB;
    }

    @Override
    public void sendMessage(@NonNull final VaspMessage message) {
        if (message instanceof SessionRequest) {
            val sessionRequest = (SessionRequest) message;
            sessionRequest.setVaspInfo(owner.vaspInfo);

            val header = sessionRequest.getHeader();
            header.setMessageId(VaspUtils.newMessageId());
            header.setSessionId(sessionId());
            header.setResponseCode(VaspResponseCode.OK.id);

            sessionRequest.setHandshake(SessionMessage.Handshake.builder()
                    .topicA(topicA)
                    .sessionPublicKey(sessionKeyPair.getPublicKey())
                    .build());

            val contract = contractService().getVaspContractInfo(peerVaspCode());

            messageService().send(
                    peerVaspCode().toTopic(),
                    EncryptionType.ASSYMETRIC,
                    contract.getHandshakeKey(),
                    sessionRequest);

            return;
        }

        if (message instanceof TransferDispatch) {
            val transferDispatch = (TransferDispatch) message;
            if (OK.id.equals(transferDispatch.getResponseCode())) {
                transferInfo.setTx(transferDispatch.getTx());
            }
        }

        super.sendMessage(message);
    }

    @Override
    public void startTransfer() {
        sendMessage(new SessionRequest());
    }

    @Override
    public void onReceiveMessage(@NonNull TopicEvent event) {
        val message = event.getMessage();
        val messageCode = message.getResponseCode();

        if (message instanceof SessionReply) {
            val sessionReply = (SessionReply) message;
            if (OK.id.equals(messageCode)) {
                topicB = sessionReply.getHandshake().getTopicB();
            }
            this.peerVaspInfo = sessionReply.getVaspInfo();
        }

        if (message instanceof TransferReply) {
            val transferReply = (TransferReply) message;
            if (OK.id.equals(messageCode)) {
                transferInfo.getTransfer().setDestinationAddress(transferReply.getTransfer().getDestinationAddress());
            }
        }

        super.onReceiveMessage(event);
    }

    @Override
    public void remove() {
        owner.removeOriginatorSession(this);
    }

    @Override
    void buildState(final SessionStateImpl.SessionStateImplBuilder builder) {
        super.buildState(builder);
        builder.type(Type.ORIGINATOR);
    }

}
