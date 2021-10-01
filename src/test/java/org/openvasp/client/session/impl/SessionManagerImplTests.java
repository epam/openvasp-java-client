package org.openvasp.client.session.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openvasp.client.config.VaspConfig;
import org.openvasp.client.model.Beneficiary;
import org.openvasp.client.model.EthAddr;
import org.openvasp.client.model.Originator;
import org.openvasp.client.model.SessionMessage;
import org.openvasp.client.model.SessionReply;
import org.openvasp.client.model.SessionRequest;
import org.openvasp.client.model.Topic;
import org.openvasp.client.model.TransferDispatch;
import org.openvasp.client.model.TransferInfo;
import org.openvasp.client.model.TransferReply;
import org.openvasp.client.model.TransferRequest;
import org.openvasp.client.model.Vaan;
import org.openvasp.client.model.VaspCode;
import org.openvasp.client.model.VaspContractInfo;
import org.openvasp.client.model.VaspInfo;
import org.openvasp.client.model.VaspMessage;
import org.openvasp.client.model.VaspResponseCode;
import org.openvasp.client.service.ContractService;
import org.openvasp.client.service.MessageService;
import org.openvasp.client.service.TopicEvent;
import org.openvasp.client.session.BeneficiarySession;
import org.openvasp.client.session.OriginatorSession;
import org.openvasp.client.session.Session;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SessionManagerImplTests {

    @Mock
    ContractService contractService;
    @Mock
    MessageService messageService;

    VaspConfig vaspConfig;
    VaspInfo vaspInfo;
    EthAddr ethAddr;
    SessionManagerImpl sessionManager;
    OriginatorSessionImpl originatorSession;
    BeneficiarySessionImpl beneficiarySession;

    @BeforeEach
    public void init() {
        ethAddr = new EthAddr("0x6befaf0656b953b188a0ee3bf3db03d07dface61");
        vaspInfo = new VaspInfo();
        vaspInfo.setVaspId(ethAddr);

        vaspConfig = new VaspConfig();
        vaspConfig.setVaspInfo(vaspInfo);
        vaspConfig.setVaspCode(new VaspCode("7dface61"));
        vaspConfig.setHandshakePrivateKey("0xe7578145d518e5272d660ccfdeceedf2d55b90867f2b7a6e54dc726662aebac2");
        sessionManager = new SessionManagerImpl(vaspConfig, contractService, messageService);

        originatorSession = (OriginatorSessionImpl) createOriginatorSession();
        beneficiarySession = (BeneficiarySessionImpl) createBeneficiarySession();
    }

    @Test
    public void createOriginatorSessionTest() {
        Assertions.assertEquals(Optional.of(originatorSession), sessionManager.getOriginatorSession(originatorSession.sessionId()));
    }

    @Test
    public void createBeneficiarySessionTest() {
        Assertions.assertEquals(Optional.of(beneficiarySession), sessionManager.getBeneficiarySession(beneficiarySession.sessionId()));
    }

    @Test
    public void restoreSessionStateTest() {
        String id = "0xdc22ceb196a83a41ac9848cd11865cb2";

        SessionState sessionState = new SessionState();
        sessionState.setType(SessionState.Type.ORIGINATOR);
        sessionState.setId(id);
        Session session = sessionManager.restoreSession(sessionState);
        Assertions.assertTrue(session instanceof OriginatorSessionImpl);

        sessionState = new SessionState();
        sessionState.setType(SessionState.Type.BENEFICIARY);
        sessionState.setId(id);
        session = sessionManager.restoreSession(sessionState);
        Assertions.assertTrue(session instanceof BeneficiarySessionImpl);
    }

    @Test
    public void waitForNoActiveSessionTest() {
        sessionManager.removeBenefeciarySession(beneficiarySession);
        sessionManager.removeOriginatorSession(originatorSession);
        Assertions.assertTrue(sessionManager.waitForNoActiveSessions(1L));
    }

    @Test
    public void waitForBeneficiarySessionTest() {
        Assertions.assertEquals(Optional.of(beneficiarySession), sessionManager.waitForBeneficiarySession(beneficiarySession.sessionId(),1L));
    }

    @Test
    public void sessionInfoTest() {
        VaspCode vaspCode = new VaspCode("7dface61");
        Assertions.assertEquals(vaspCode, sessionManager.vaspCode());
        Assertions.assertEquals(vaspCode, originatorSession.vaspInfo().getVaspCode());
        Assertions.assertEquals(TransferRequest.VirtualAssetType.BTC, originatorSession.transferInfo().getTransfer().getAssetType());
        Assertions.assertNull(originatorSession.peerVaspInfo());
    }

    @Test
    public void resolveSenderVaspIdTest() {
        sessionManager.removeBenefeciarySession(beneficiarySession);

        SessionMessage vaspMessage = createSessionRequest();
        Assertions.assertEquals(Optional.of(ethAddr), sessionManager.resolveSenderVaspId(vaspMessage));

        SessionMessage sessionReply = createSessionReply();
        Assertions.assertEquals(Optional.of(ethAddr), sessionManager.resolveSenderVaspId(sessionReply));

        VaspMessage transferRequest = createTransferRequest();
        Assertions.assertEquals(Optional.empty(), sessionManager.resolveSenderVaspId(transferRequest));
    }

    @Test
    public void sendMessageBeneficiaryTest() {
        SessionMessage vaspMessage = createSessionReply();
        beneficiarySession.sendMessage(vaspMessage);
        verify(messageService).send(any(),any(),any(),any());
    }

    @Test
    public void onReceiveMessageOriginatorTest() {
        Topic topic = new Topic("0xe6f72a9c");

        TopicEvent<VaspMessage> topicEvent = new TopicEvent<>(topic, createSessionReply());
        originatorSession.onReceiveMessage(topicEvent);
        Assertions.assertEquals(new EthAddr("0x6befaf0656b953b188a0ee3bf3db03d07dface61"), originatorSession.getState().getPeerVaspInfo().getVaspId());

        topicEvent = new TopicEvent<>(topic, createTransferReply());
        originatorSession.onReceiveMessage(topicEvent);
        Assertions.assertEquals("0x6befaf0656b953b188a0ee3bf3db03d07dface61", originatorSession.getState().getTransferInfo().getDestinationAddress());
    }

    @Test
    public void onReceiveMessageBeneficiaryTest() {
        Topic topic = new Topic("0xe6f72a9c");

        TopicEvent<VaspMessage> topicEvent = new TopicEvent<>(topic, createTransferRequest());
        beneficiarySession.onReceiveMessage(topicEvent);
        Assertions.assertEquals(createBeneficiary(), beneficiarySession.getState().getTransferInfo().getBeneficiary());

        topicEvent = new TopicEvent<>(topic, createTransferDispatch());
        beneficiarySession.onReceiveMessage(topicEvent);
        Assertions.assertEquals("tx0001", beneficiarySession.getState().getTransferInfo().getTx().getId());
    }

    @Test
    public void startTransferTest() {
        originatorSession.startTransfer();
        verify(messageService).send(any(),any(),any(),any());
    }

    @Test
    public void takeIncomingMessageTest() {
        Assertions.assertEquals(Optional.empty(), originatorSession.takeIncomingMessage(1L));

        VaspMessage vaspMessage = createSessionReply();
        originatorSession.addToIncomingQueue(vaspMessage);
        Assertions.assertEquals(Optional.of(vaspMessage), originatorSession.takeIncomingMessage(1L));
    }

    private OriginatorSession createOriginatorSession() {
        TransferInfo transferInfo = new TransferInfo();
        transferInfo.setBeneficiary(createBeneficiary());
        TransferRequest.Transfer transfer = new TransferRequest.Transfer();
        transfer.setAssetType(TransferRequest.VirtualAssetType.BTC);
        transferInfo.setTransfer(transfer);

        VaspContractInfo vaspContractInfo = new VaspContractInfo();
        vaspContractInfo.setHandshakeKey("0x044ffeb548a05b879757dc37911bb41f95966311b00b8687da8913c6b0bf7deddd1c233d8294e0c31f0099058bf056c0f5fb8919e87bf148b0f04ace8f19004bb9");

        when(contractService.getVaspContractInfo((VaspCode) any())).thenReturn(vaspContractInfo);
        return sessionManager.createOriginatorSession(transferInfo);
    }

    private BeneficiarySession createBeneficiarySession() {
        return new BeneficiarySessionImpl(sessionManager, (SessionRequest) createSessionRequest());
    }

    private SessionMessage createSessionRequest() {
        SessionRequest vaspMessage = new SessionRequest();
        vaspMessage.setHeader(createHeader(VaspMessage.TypeDescriptor.SESSION_REQUEST));
        vaspMessage.setVaspInfo(vaspInfo);

        SessionRequest.Handshake handshake = new SessionRequest.Handshake();
        handshake.setSessionPublicKey("0x044ffeb548a05b879757dc37911bb41f95966311b00b8687da8913c6b0bf7deddd1c233d8294e0c31f0099058bf056c0f5fb8919e87bf148b0f04ace8f19004bb9");
        handshake.setTopicA(new Topic("0x527aeb21"));
        vaspMessage.setHandshake(handshake);
        return vaspMessage;
    }

    private SessionMessage createSessionReply() {
        SessionReply vaspMessage = new SessionReply();
        vaspMessage.setHeader(createHeader(VaspMessage.TypeDescriptor.SESSION_REPLY));
        vaspMessage.setVaspInfo(vaspInfo);
        return vaspMessage;
    }

    private VaspMessage createTransferRequest() {
        TransferRequest vaspMessage = new TransferRequest();
        vaspMessage.setHeader(createHeader(VaspMessage.TypeDescriptor.TRANSFER_REQUEST));
        vaspMessage.setBeneficiary(createBeneficiary());
        vaspMessage.setOriginator(createOriginator());
        return vaspMessage;
    }

    private VaspMessage createTransferReply() {
        TransferReply vaspMessage = new TransferReply();
        vaspMessage.setHeader(createHeader(VaspMessage.TypeDescriptor.TRANSFER_REPLY));
        vaspMessage.setDestinationAddress("0x6befaf0656b953b188a0ee3bf3db03d07dface61");
        return vaspMessage;
    }

    private VaspMessage createTransferDispatch() {
        TransferDispatch vaspMessage = new TransferDispatch();
        vaspMessage.setHeader(createHeader(VaspMessage.TypeDescriptor.TRANSFER_DISPATCH));
        TransferDispatch.Tx tx = new TransferDispatch.Tx();
        tx.setId("tx0001");
        vaspMessage.setTx(tx);
        return vaspMessage;
    }

    private VaspMessage.Header createHeader(final VaspMessage.TypeDescriptor typeDescriptor) {
        VaspMessage.Header header = new VaspMessage.Header();
        header.setMessageType(typeDescriptor);
        header.setMessageId("eaae0fcbf6a342aec65936ea208653");
        header.setSessionId("0xfe3f216d0de7f94ba978225842c7330c");
        header.setResponseCode(VaspResponseCode.OK.id);
        return header;
    }

    private Originator createOriginator() {
        Originator originator = new Originator();
        originator.setName("Average Joe");
        originator.setVaan(new Vaan("10007dface610000000001e7"));
        return originator;
    }

    private Beneficiary createBeneficiary() {
        return new Beneficiary("John Smith", new Vaan("1000a0b1c2d3c2d3e4f5ffce"));
    }
}
