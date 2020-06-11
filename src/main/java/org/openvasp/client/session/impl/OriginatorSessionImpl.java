package org.openvasp.client.session.impl;

import lombok.NonNull;
import lombok.val;
import org.openvasp.client.common.VaspUtils;
import org.openvasp.client.crypto.ECDHKeyPair;
import org.openvasp.client.model.*;
import org.openvasp.client.service.TopicEvent;
import org.openvasp.client.session.OriginatorSession;

import static com.google.common.base.Preconditions.checkArgument;
import static org.openvasp.client.model.VaspResponseCode.OK;

/**
 * @author Olexandr_Bilovol@epam.com
 */
final class OriginatorSessionImpl extends AbstractSession implements OriginatorSession {

    private final long topicListenerId;
    private final String sessionPublicKey;

    OriginatorSessionImpl(
            @NonNull final SessionManagerImpl owner,
            @NonNull final TransferInfo transferInfo) {

        super(owner, VaspUtils.newSessionId());

        // 6 items to init, the same as in BeneficiarySessionImpl
        this.peerVaspInfo = null;
        this.transferInfo = transferInfo;
        this.topicA = Topic.newRandom();
        this.topicB = null;
        val sessionKeyPair = ECDHKeyPair.generateKeyPair();
        val contract = contractService().getVaspContractInfo(transferInfo.getBeneficiary().getVaan().getVaspCode());
        this.sharedSecret = sessionKeyPair.generateSharedSecretHex(contract.getHandshakeKey());
        this.sessionPublicKey = sessionKeyPair.getPublicKey();

        this.setMessageHandler(owner.messageHandler);

        owner.addOriginatorSession(this);

        this.topicListenerId = owner.messageService.addTopicListener(
                incomingMessageTopic(),
                EncryptionType.SYMMETRIC,
                sharedSecret,
                this::onReceiveMessage);
    }

    OriginatorSessionImpl(
            @NonNull final SessionManagerImpl owner,
            @NonNull final SessionState sessionState) {

        super(owner, sessionState.getId());

        // 6 items to init, the same as in BeneficiarySessionImpl
        this.peerVaspInfo = sessionState.getPeerVaspInfo();
        this.transferInfo = sessionState.getTransferInfo();
        this.topicA = sessionState.getIncomingTopic();
        this.topicB = sessionState.getOutgoingTopic();
        this.sharedSecret = sessionState.getSharedSecret();
        this.sessionPublicKey = sessionState.getSessionPublicKey();

        this.setMessageHandler(owner.messageHandler);

        owner.addOriginatorSession(this);

        this.topicListenerId = owner.messageService.addTopicListener(
                incomingMessageTopic(),
                EncryptionType.SYMMETRIC,
                sharedSecret,
                this::onReceiveMessage);
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
        checkArgument(
                !(message instanceof SessionRequest),
                "SessionRequest must be sent only by 'startTransfer' method.");

        super.sendMessage(message);
    }

    @Override
    public void startTransfer() {
        val sessionRequest = new SessionRequest();

        // Header
        val header = sessionRequest.getHeader();
        header.setMessageId(VaspUtils.newMessageId());
        header.setSessionId(sessionId());
        header.setResponseCode(VaspResponseCode.OK.id);

        // VaspInfo
        sessionRequest.setVaspInfo(owner.vaspInfo);

        // Handshake
        sessionRequest.setHandshake(new SessionRequest.Handshake(topicA, sessionPublicKey));

        // Beneficiary contract
        val beneficiaryVaspCode = transferInfo.getBeneficiary().getVaan().getVaspCode();
        val beneficiaryContract = contractService().getVaspContractInfo(beneficiaryVaspCode);

        // Send message by MessageService
        messageService().send(
                beneficiaryVaspCode.toTopic(),
                EncryptionType.ASSYMETRIC,
                beneficiaryContract.getHandshakeKey(),
                sessionRequest);
    }

    @Override
    void onReceiveMessage(@NonNull final TopicEvent<VaspMessage> event) {
        val message = event.getPayload();
        val messageCode = message.getResponseCode();

        if (message instanceof SessionReply) {
            val sessionReply = (SessionReply) message;
            if (OK.id.equals(messageCode)) {
                topicB = sessionReply.getHandshake().getTopicB();
            }
            this.peerVaspInfo = sessionReply.getVaspInfo();
        }

        if (message instanceof TransferReply) {
            val sessionReply = (TransferReply) message;
            if (OK.id.equals(messageCode)) {
                transferInfo.setDestinationAddress(sessionReply.getDestinationAddress());
            }
        }

        super.onReceiveMessage(event);
    }

    @Override
    public void remove() {
        owner.messageService.removeTopicListener(incomingMessageTopic(), topicListenerId);
        owner.removeOriginatorSession(this);
    }

    @Override
    void buildState(final SessionState.SessionStateBuilder builder) {
        super.buildState(builder);
        builder.type(SessionState.Type.ORIGINATOR)
                .sessionPublicKey(sessionPublicKey);
    }

}
