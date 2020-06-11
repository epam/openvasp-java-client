package org.openvasp.transfer.recording;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openvasp.client.VaspInstance;
import org.openvasp.client.common.ExceptionHandler;
import org.openvasp.client.common.Json;
import org.openvasp.client.common.TestConstants;
import org.openvasp.client.config.VaspModule;
import org.openvasp.client.model.*;
import org.openvasp.client.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openvasp.client.common.TestConstants.*;
import static org.openvasp.client.model.VaspResponseCode.OK;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public abstract class BaseRecordingTransferIT {

    private final Logger log = LoggerFactory.getLogger(getClass());

    final VaspModule
            module1,
            module2;

    VaspInstance
            instance1,
            instance2;

    final TransferInfo
            transferA = Json.loadTestJson(TransferInfo.class, "transfer/recording/transfer-info-a.json"),
            transferB = Json.loadTestJson(TransferInfo.class, "transfer/recording/transfer-info-b.json");

    public BaseRecordingTransferIT(
            @NonNull final VaspModule module1,
            @NonNull final VaspModule module2) {

        this.module1 = module1;
        this.module2 = module2;
    }

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        instance1 = new VaspInstance(module1);
        instance1.startSessionManager();
        instance2 = new VaspInstance(module2);
        instance2.startSessionManager();
        Thread.sleep(WAIT_TIMEOUT_3);
    }

    @AfterEach
    @SneakyThrows
    public void tearDown() {
        instance1.close();
        instance2.close();
    }

    public void checkCallbackStyleTransfer() {
        val transferLog = Collections.synchronizedList(new ArrayList<TransferLogRecord>());
        val messageHandler = new RecordingTestHandler(transferLog);

        final ExceptionHandler exceptionHandler =
                (exception) -> {
                    log.error("Error while executing scenario: ", exception);
                };

        final Consumer<VaspMessage> confirmationHandler =
                (message) -> {
                    log.error("Message ID = {} confirmed", message.getHeader().getMessageId());
                };

        instance1.setMessageHandler(messageHandler);
        instance1.setExceptionHandler(exceptionHandler);
        instance1.setConfirmationHandler(confirmationHandler);
        instance2.setMessageHandler(messageHandler);
        instance2.setExceptionHandler(exceptionHandler);
        instance2.setConfirmationHandler(confirmationHandler);

        // Initiate transfer instance1 => instance2
        val originatorSessionA = instance1.createOriginatorSession(transferA);
        originatorSessionA.startTransfer();

        // Wait for the transfer to be completed
        assertThat(instance1.waitForNoActiveSessions(WAIT_TIMEOUT_1)).isTrue();
        assertThat(instance2.waitForNoActiveSessions(WAIT_TIMEOUT_2)).isTrue();

        checkTransfer(messageHandler.transferLog, TestConstants.VASP_CODE_1, TestConstants.VASP_CODE_2);

        // Clear transfer log before the second transfer
        transferLog.clear();

        // Initiate transfer instance2 => instance1
        val originatorSessionB = instance2.createOriginatorSession(transferB);
        originatorSessionB.startTransfer();

        // Wait for the transfer to be completed
        assertThat(instance2.waitForNoActiveSessions(WAIT_TIMEOUT_1)).isTrue();
        assertThat(instance1.waitForNoActiveSessions(WAIT_TIMEOUT_2)).isTrue();

        checkTransfer(messageHandler.transferLog, TestConstants.VASP_CODE_2, TestConstants.VASP_CODE_1);
    }

    public void checkWaitingStyleTransfer() {
        val transferLog = Collections.synchronizedList(new ArrayList<TransferLogRecord>());

        // Initiate transfer instance1 => instance2
        val originatorSession = instance1.createOriginatorSession(transferA);
        originatorSession.startTransfer();

        // Wait for the beneficiary session to be created
        val beneficiarySessionOpt = instance2.waitForBeneficiarySession(originatorSession.sessionId(), WAIT_TIMEOUT_2);
        assertThat(beneficiarySessionOpt).isNotEmpty();
        val beneficiarySession = beneficiarySessionOpt.get();

        // The 1st incoming message
        val message = beneficiarySession.takeIncomingMessage(WAIT_TIMEOUT_2);
        assertThat(message).isNotEmpty();
        assertThat(message.get()).isInstanceOf(SessionRequest.class);
        assertThat(beneficiarySession.vaspInfo()).isNotNull();
        assertThat(beneficiarySession.peerVaspInfo()).isNotNull();

        transferLog.add(new TransferLogRecord(
                beneficiarySession.vaspInfo().getVaspCode(),
                beneficiarySession.peerVaspInfo().getVaspCode(),
                message.get().asSessionRequest()));

        // The 1st response
        val sessionReply = new SessionReply();
        sessionReply.getHeader().setResponseCode(VaspResponseCode.OK.id);
        beneficiarySession.sendMessage(sessionReply);

        checkWaitingStyleStep(
                originatorSession,
                SessionReply.class,
                transferLog,
                (request, session) -> {
                    val response = new TransferRequest();
                    val transferInfo = session.transferInfo();
                    response.getHeader().setResponseCode(OK.id);
                    response.setOriginator(transferInfo.getOriginator());
                    response.setBeneficiary(transferInfo.getBeneficiary());
                    response.setTransfer(transferInfo.getTransfer());
                    return response;
                });

        checkWaitingStyleStep(
                beneficiarySession,
                TransferRequest.class,
                transferLog,
                (request, session) -> {
                    val response = new TransferReply();
                    response.getHeader().setResponseCode(OK.id);
                    response.setDestinationAddress("destination-address");
                    return response;
                });

        checkWaitingStyleStep(
                originatorSession,
                TransferReply.class,
                transferLog,
                (request, session) -> {
                    val response = new TransferDispatch();
                    response.getHeader().setResponseCode(OK.id);
                    val tx = new TransferDispatch.Tx();
                    tx.setId("tx-id");
                    tx.setDateTime(ZonedDateTime.now());
                    tx.setSendingAddress("sending-address");
                    response.setTx(tx);
                    return response;
                });

        checkWaitingStyleStep(
                beneficiarySession,
                TransferDispatch.class,
                transferLog,
                (request, session) -> {
                    val response = new TransferConfirmation();
                    response.getHeader().setResponseCode(OK.id);
                    return response;
                });

        checkWaitingStyleStep(
                originatorSession,
                TransferConfirmation.class,
                transferLog,
                (request, session) -> {
                    val response = new TerminationMessage();
                    response.getHeader().setResponseCode(OK.id);
                    return response;
                });

        checkWaitingStyleStep(
                beneficiarySession,
                TerminationMessage.class,
                transferLog,
                (request, session) -> null);

        originatorSession.remove();
        beneficiarySession.remove();

        checkTransfer(transferLog, TestConstants.VASP_CODE_1, TestConstants.VASP_CODE_2);
    }

    private void checkTransfer(
            final List<TransferLogRecord> transferLog,
            final VaspCode originatorVaspCode,
            final VaspCode beneficiaryVaspCode) {

        assertThat(transferLog).hasSize(7);

        // The 1st message is SessionRequest and it is processed at the beneficiary side
        assertThat(transferLog.get(0).vaspCode).isEqualTo(beneficiaryVaspCode);
        assertThat(transferLog.get(0).vaspMessage).isInstanceOf(SessionRequest.class);
        val message0 = transferLog.get(0).vaspMessage.asSessionRequest();
        // The sender is the originator
        assertThat(message0.getSenderVaspCode()).isEqualTo(originatorVaspCode);

        // The 2nd message is SessionReply and it is processed at the originator side
        assertThat(transferLog.get(1).vaspCode).isEqualTo(originatorVaspCode);
        assertThat(transferLog.get(1).vaspMessage).isInstanceOf(SessionReply.class);
        val message1 = transferLog.get(1).vaspMessage.asSessionReply();
        // The sender is the beneficiary
        assertThat(message1.getSenderVaspCode()).isEqualTo(beneficiaryVaspCode);

        // The 3d message is TransferRequest and it is processed at the beneficiary side
        assertThat(transferLog.get(2).vaspCode).isEqualTo(beneficiaryVaspCode);
        assertThat(transferLog.get(2).vaspMessage).isInstanceOf(TransferRequest.class);
        val message2 = transferLog.get(2).vaspMessage.asTransferRequest();
        // The sender is the originator
        assertThat(transferLog.get(2).peerVaspCode).isEqualTo(originatorVaspCode);

        // The 4th message is TransferReply and it is processed at the originator side
        assertThat(transferLog.get(3).vaspCode).isEqualTo(originatorVaspCode);
        assertThat(transferLog.get(3).vaspMessage).isInstanceOf(TransferReply.class);
        val message3 = transferLog.get(3).vaspMessage.asTransferReply();
        // The sender is the beneficiary
        assertThat(transferLog.get(3).peerVaspCode).isEqualTo(beneficiaryVaspCode);

        // The 5th message is TransferDispatch and it is processed at the beneficiary side
        assertThat(transferLog.get(4).vaspCode).isEqualTo(beneficiaryVaspCode);
        assertThat(transferLog.get(4).vaspMessage).isInstanceOf(TransferDispatch.class);
        val message4 = transferLog.get(4).vaspMessage.asTransferDispatch();
        // The sender is the originator
        assertThat(transferLog.get(4).peerVaspCode).isEqualTo(originatorVaspCode);

        // The 6th message is TransferConfirmation and it is processed at the originator side
        assertThat(transferLog.get(5).vaspCode).isEqualTo(originatorVaspCode);
        assertThat(transferLog.get(5).vaspMessage).isInstanceOf(TransferConfirmation.class);
        val message5 = transferLog.get(5).vaspMessage.asTransferConfirmation();
        // The sender is the beneficiary
        assertThat(transferLog.get(5).peerVaspCode).isEqualTo(beneficiaryVaspCode);

        // The 7th message is TerminationMessage and it is processed at the beneficiary side
        assertThat(transferLog.get(6).vaspCode).isEqualTo(beneficiaryVaspCode);
        assertThat(transferLog.get(6).vaspMessage).isInstanceOf(TerminationMessage.class);
        val message6 = transferLog.get(6).vaspMessage.asTerminationMessage();
        // The sender is the originator
        assertThat(transferLog.get(6).peerVaspCode).isEqualTo(originatorVaspCode);
    }

    private <T extends VaspMessage, U extends VaspMessage>
    void checkWaitingStyleStep(
            @NonNull final Session session,
            @NonNull final Class<T> expectedMessageClass,
            @NonNull final List<TransferLogRecord> transferLog,
            @NonNull final BiFunction<T, Session, U> responseAction) {

        val messageOpt = session.takeIncomingMessage(WAIT_TIMEOUT_2);
        assertThat(messageOpt).isNotEmpty();
        val message = messageOpt.get();
        assertThat(message).isInstanceOf(expectedMessageClass);

        transferLog.add(new TransferLogRecord(
                session.vaspInfo().getVaspCode(),
                session.peerVaspInfo().getVaspCode(),
                message));

        @SuppressWarnings("unchecked")
        val response = responseAction.apply((T) message, session);

        if (response != null) {
            session.sendMessage(response);
        }
    }

}
