package org.openvasp.client.session.impl;

import lombok.NonNull;
import lombok.val;
import org.openvasp.client.crypto.ECDHKeyPair;
import org.openvasp.client.model.*;
import org.openvasp.client.service.TopicEvent;
import org.openvasp.client.session.BeneficiarySession;

/**
 * @author Olexandr_Bilovol@epam.com
 */
final class BeneficiarySessionImpl extends AbstractSession implements BeneficiarySession {

    public BeneficiarySessionImpl(
            @NonNull final VaspInstanceImpl owner,
            @NonNull final SessionRequest sessionRequest) {

        super(owner, sessionRequest.getHeader().getSessionId());

        // 6 items to init, the as in OriginatorSessionImpl
        this.peerVaspInfo = sessionRequest.getVaspInfo();
        this.transferInfo = new TransferInfo();
        this.topicA = sessionRequest.getHandshake().getTopicA();
        this.topicB = Topic.newRandom();
        this.sessionKeyPair = ECDHKeyPair.importPrivateKey(owner.handshakePrivateKey);
        this.sharedSecret = sessionKeyPair.generateSharedSecretHex(sessionRequest.getHandshake().getSessionPublicKey());
    }

    @Override
    public VaspCode peerVaspCode() {
        return peerVaspInfo.getVaspCode();
    }

    @Override
    String sharedSecret() {
        return sharedSecret;
    }

    @Override
    Topic incomingMessageTopic() {
        return topicB;
    }

    @Override
    Topic outgoingMessageTopic() {
        return topicA;
    }

    @Override
    public void sendMessage(@NonNull final VaspMessage message) {
        val messageCode = message.getResponseCode();

        if (message instanceof SessionReply) {
            val sessionReply = (SessionReply) message;
            sessionReply.setHandshake(SessionMessage.Handshake.builder()
                    .topicB(topicB)
                    .build());
        }

        if (messageCode.equals(VaspResponseCode.OK.id) && message instanceof TransferMessage) {
            val transferMessage = (TransferMessage) message;
            transferInfo.setTransfer(transferMessage.getTransfer());
        }

        super.sendMessage(message);
    }

    @Override
    public void onReceiveMessage(@NonNull final TopicEvent event) {
        val message = event.getMessage();

        if (message instanceof TransferRequest) {
            val transferRequest = (TransferRequest) message;
            transferInfo.setOriginator(transferRequest.getOriginator());
            transferInfo.setBeneficiary(transferRequest.getBeneficiary());
            transferInfo.setTransfer(transferRequest.getTransfer());
        }

        if (message instanceof TransferDispatch) {
            val transferDispatch = (TransferDispatch) message;
            transferInfo.setOriginator(transferDispatch.getOriginator());
            transferInfo.setBeneficiary(transferDispatch.getBeneficiary());
            transferInfo.setTransfer(transferDispatch.getTransfer());
            transferInfo.setTx(transferDispatch.getTx());
        }

        super.onReceiveMessage(event);
    }

    @Override
    public void remove() {
        owner.removeBenefeciarySession(this);
    }

}
