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

    private final long topicListenerId;

    BeneficiarySessionImpl(
            @NonNull final SessionManagerImpl owner,
            @NonNull final SessionRequest sessionRequest) {

        super(owner, sessionRequest.getHeader().getSessionId());

        this.peerVaspInfo = sessionRequest.getVaspInfo();
        this.transferInfo = new TransferInfo();
        this.topicA = sessionRequest.getHandshake().getTopicA();
        this.topicB = Topic.newRandom();
        val sessionKeyPair = ECDHKeyPair.importPrivateKey(owner.handshakePrivateKey);
        this.sharedSecret = sessionKeyPair.generateSharedSecretHex(sessionRequest.getSessionPublicKey());

        this.setMessageHandler(owner.messageHandler);

        owner.addBenefeciarySession(this);

        this.topicListenerId = owner.messageService.addTopicListener(
                incomingMessageTopic(),
                EncryptionType.SYMMETRIC,
                sharedSecret,
                this::onReceiveMessage);
    }

    BeneficiarySessionImpl(
            @NonNull final SessionManagerImpl owner,
            @NonNull final SessionState sessionState) {

        super(owner, sessionState.getId());

        this.peerVaspInfo = sessionState.getPeerVaspInfo();
        this.transferInfo = sessionState.getTransferInfo();
        this.topicA = sessionState.getOutgoingTopic();
        this.topicB = sessionState.getIncomingTopic();
        this.sharedSecret = sessionState.getSharedSecret();

        this.setMessageHandler(owner.messageHandler);

        owner.addBenefeciarySession(this);

        this.topicListenerId = owner.messageService.addTopicListener(
                incomingMessageTopic(),
                EncryptionType.SYMMETRIC,
                sharedSecret,
                this::onReceiveMessage);
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
        if (message instanceof SessionReply) {
            val sessionReply = (SessionReply) message;
            sessionReply.setHandshake(new SessionReply.Handshake(topicB));
            sessionReply.setVaspInfo(owner.vaspInfo);
        }

        super.sendMessage(message);
    }

    @Override
    public void onReceiveMessage(@NonNull final TopicEvent<VaspMessage> event) {
        val message = event.getPayload();

        if (message instanceof TransferRequest) {
            val transferRequest = (TransferRequest) message;
            transferInfo.setOriginator(transferRequest.getOriginator());
            transferInfo.setBeneficiary(transferRequest.getBeneficiary());
            transferInfo.setTransfer(transferRequest.getTransfer());
        }

        if (message instanceof TransferDispatch) {
            val transferDispatch = (TransferDispatch) message;
            transferInfo.setTx(transferDispatch.getTx());
        }

        super.onReceiveMessage(event);
    }

    @Override
    public void remove() {
        owner.messageService.removeTopicListener(incomingMessageTopic(), topicListenerId);
        owner.removeBenefeciarySession(this);
    }

    @Override
    void buildState(final SessionState.SessionStateBuilder builder) {
        super.buildState(builder);
        builder.type(SessionState.Type.BENEFICIARY);
    }

}
