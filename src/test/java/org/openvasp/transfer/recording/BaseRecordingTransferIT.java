package org.openvasp.transfer.recording;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openvasp.client.VaspClient;
import org.openvasp.client.common.Json;
import org.openvasp.client.common.TestConstants;
import org.openvasp.client.common.VaspException;
import org.openvasp.client.config.VaspModule;
import org.openvasp.client.model.*;
import org.openvasp.client.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openvasp.client.common.TestConstants.WAIT_TIMEOUT_1;
import static org.openvasp.client.common.TestConstants.WAIT_TIMEOUT_2;
import static org.openvasp.client.model.VaspResponseCode.OK;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public abstract class BaseRecordingTransferIT {

    private final Logger log = LoggerFactory.getLogger(getClass());

    final VaspModule
            module1,
            module2;

    VaspClient
            client1,
            client2;

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
    public void setUp() {
        client1 = new VaspClient(module1);
        client2 = new VaspClient(module2);
    }

    @AfterEach
    @SneakyThrows
    public void tearDown() {
        client1.close();
        client2.close();
    }

    public void checkCallbackStyleTransfer() {
        val transferLog = Collections.synchronizedList(new ArrayList<TransferLogRecord>());
        val messageHandler = new RecordingTestHandler(transferLog);
        final BiConsumer<VaspException, Session> errorLogger =
                (exception, session) -> { log.error("Error while executing scenario: ", exception); };

        client1.setCustomMessageHandler(messageHandler);
        client1.setCustomErrorHandler(errorLogger);
        client2.setCustomMessageHandler(messageHandler);
        client2.setCustomErrorHandler(errorLogger);

        // Initiate transfer client1 => client2
        val originatorSessionA = client1.createOriginatorSession(transferA);
        originatorSessionA.startTransfer();

        // Wait for the transfer to be completed
        assertThat(client1.waitForNoActiveSessions(WAIT_TIMEOUT_1)).isTrue();
        assertThat(client2.waitForNoActiveSessions(WAIT_TIMEOUT_2)).isTrue();

        checkTransfer(messageHandler.transferLog, TestConstants.VASP_CODE_1, TestConstants.VASP_CODE_2, transferA);

        // Clear transfer log before the second transfer
        transferLog.clear();

        // Initiate transfer client2 => client1
        val originatorSessionB = client2.createOriginatorSession(transferB);
        originatorSessionB.startTransfer();

        // Wait for the transfer to be completed
        assertThat(client2.waitForNoActiveSessions(WAIT_TIMEOUT_1)).isTrue();
        assertThat(client1.waitForNoActiveSessions(WAIT_TIMEOUT_2)).isTrue();

        checkTransfer(messageHandler.transferLog, TestConstants.VASP_CODE_2, TestConstants.VASP_CODE_1, transferB);
    }

    @Test
    public void checkWaitingStyleTransfer() {
        val transferLog = Collections.synchronizedList(new ArrayList<TransferLogRecord>());

        // Initiate transfer client1 => client2
        val originatorSession = client1.createOriginatorSession(transferA);
        originatorSession.startTransfer();

        // Wait for the beneficiary session to be created
        val beneficiarySessionOpt = client2.waitForBeneficiarySession(originatorSession.sessionId(), WAIT_TIMEOUT_2);
        assertThat(beneficiarySessionOpt).isNotEmpty();
        val beneficiarySession = beneficiarySessionOpt.get();

        // The 1st incoming message
        assertThat(beneficiarySession.incomingMessages()).hasSize(1);
        assertThat(beneficiarySession.lastIncomingMessage()).isInstanceOf(SessionRequest.class);
        val sessionRequest = beneficiarySession.lastIncomingMessage(SessionRequest.class);
        transferLog.add(new TransferLogRecord(beneficiarySession.vaspCode(), sessionRequest));
        // The 1st response
        val sessionReply = new SessionReply();
        sessionReply.getHeader().setResponseCode(VaspResponseCode.OK.id);
        beneficiarySession.sendMessage(sessionReply);

        checkWaitingStyleStep(
                originatorSession,
                SessionReply.class,
                1,
                transferLog,
                (request, session) -> {
                    val response = new TransferRequest();
                    response.getHeader().setResponseCode(OK.id);
                    response.setOriginator(session.transferInfo().getOriginator());
                    response.setBeneficiary(session.transferInfo().getBeneficiary());
                    response.setTransfer(session.transferInfo().getTransfer());
                    return response;
                });

        checkWaitingStyleStep(
                beneficiarySession,
                TransferRequest.class,
                2,
                transferLog,
                (request, session) -> {
                    val response = new TransferReply();
                    response.getHeader().setResponseCode(OK.id);
                    response.setOriginator(request.getOriginator());
                    response.setBeneficiary(request.getBeneficiary());
                    response.setTransfer(request.getTransfer());
                    return response;
                });

        checkWaitingStyleStep(
                originatorSession,
                TransferReply.class,
                2,
                transferLog,
                (request, session) -> {
                    val response = new TransferDispatch();
                    response.getHeader().setResponseCode(OK.id);
                    response.setOriginator(session.transferInfo().getOriginator());
                    response.setBeneficiary(session.transferInfo().getBeneficiary());
                    response.setTransfer(session.transferInfo().getTransfer());
                    response.setTx(new TransferMessage.Transaction());
                    response.getTx().setDateTime(LocalDateTime.now());
                    return response;
                });

        checkWaitingStyleStep(
                beneficiarySession,
                TransferDispatch.class,
                3,
                transferLog,
                (request, session) -> {
                    val response = new TransferConfirmation();
                    response.getHeader().setResponseCode(OK.id);
                    response.setOriginator(session.transferInfo().getOriginator());
                    response.setBeneficiary(session.transferInfo().getBeneficiary());
                    response.setTransfer(session.transferInfo().getTransfer());
                    response.setTx(session.transferInfo().getTx());
                    return response;
                });

        checkWaitingStyleStep(
                originatorSession,
                TransferConfirmation.class,
                3,
                transferLog,
                (request, session) -> {
                    val response = new TerminationMessage();
                    response.getHeader().setResponseCode(OK.id);
                    return response;
                });

        checkWaitingStyleStep(
                beneficiarySession,
                TerminationMessage.class,
                4,
                transferLog,
                (request, session) -> null);

        originatorSession.remove();
        beneficiarySession.remove();

        checkTransfer(transferLog, TestConstants.VASP_CODE_1, TestConstants.VASP_CODE_2, transferA);
    }

    private void checkTransfer(
            final List<TransferLogRecord> transferLog,
            final VaspCode originatorVaspCode,
            final VaspCode beneficiaryVaspCode,
            final TransferInfo transferInfo) {

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
        assertThat(message2.getSenderVaspCode()).isEqualTo(originatorVaspCode);

        // The 4th message is TransferReply and it is processed at the originator side
        assertThat(transferLog.get(3).vaspCode).isEqualTo(originatorVaspCode);
        assertThat(transferLog.get(3).vaspMessage).isInstanceOf(TransferReply.class);
        val message3 = transferLog.get(3).vaspMessage.asTransferReply();
        // The sender is the beneficiary
        assertThat(message3.getSenderVaspCode()).isEqualTo(beneficiaryVaspCode);

        // The 5th message is TransferDispatch and it is processed at the beneficiary side
        assertThat(transferLog.get(4).vaspCode).isEqualTo(beneficiaryVaspCode);
        assertThat(transferLog.get(4).vaspMessage).isInstanceOf(TransferDispatch.class);
        val message4 = transferLog.get(4).vaspMessage.asTransferDispatch();
        // The sender is the originator
        assertThat(message4.getSenderVaspCode()).isEqualTo(originatorVaspCode);

        // The 6th message is TransferConfirmation and it is processed at the originator side
        assertThat(transferLog.get(5).vaspCode).isEqualTo(originatorVaspCode);
        assertThat(transferLog.get(5).vaspMessage).isInstanceOf(TransferConfirmation.class);
        val message5 = transferLog.get(5).vaspMessage.asTransferConfirmation();
        // The sender is the beneficiary
        assertThat(message5.getSenderVaspCode()).isEqualTo(beneficiaryVaspCode);

        // The 7th message is TerminationMessage and it is processed at the beneficiary side
        assertThat(transferLog.get(6).vaspCode).isEqualTo(beneficiaryVaspCode);
        assertThat(transferLog.get(6).vaspMessage).isInstanceOf(TerminationMessage.class);
        val message6 = transferLog.get(6).vaspMessage.asTerminationMessage();
        // The sender is the originator
        assertThat(message6.getSenderVaspCode()).isEqualTo(originatorVaspCode);
    }

    private <T extends VaspMessage, U extends VaspMessage>
    void checkWaitingStyleStep(
            @NonNull final Session session,
            @NonNull final Class<T> expectedMessageClass,
            final int expectedIncomingMessageCount,
            @NonNull final List<TransferLogRecord> transferLog,
            @NonNull final BiFunction<T, Session, U> responseAction) {

        val messageOpt = session.waitForNewMessage(expectedMessageClass, WAIT_TIMEOUT_2);
        assertThat(messageOpt).isNotEmpty();
        assertThat(session.incomingMessages()).hasSize(expectedIncomingMessageCount);
        assertThat(session.lastIncomingMessage()).isInstanceOf(expectedMessageClass);
        val message = messageOpt.get();
        transferLog.add(new TransferLogRecord(session.vaspCode(), message));
        val response = responseAction.apply(message, session);
        if (response != null) {
            session.sendMessage(response);
        }
    }

}
