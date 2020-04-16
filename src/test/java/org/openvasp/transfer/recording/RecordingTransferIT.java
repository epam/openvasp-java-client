package org.openvasp.transfer.recording;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openvasp.client.VaspClient;
import org.openvasp.client.common.Json;
import org.openvasp.client.common.TestConstants;
import org.openvasp.client.config.LocalTestModule;
import org.openvasp.client.config.VaspModule;
import org.openvasp.client.model.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openvasp.transfer.recording.RecordingTestHandler.LogRecord;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Tag("transfer")
public class RecordingTransferIT {

    final VaspModule
            module1 = LocalTestModule.module1,
            module2 = LocalTestModule.module2;

    VaspClient
            client1,
            client2;

    RecordingTestHandler
            messageHandler;

    final TransferInfo
            transferA = Json.loadTestJson(TransferInfo.class, "transfer/recording/transfer-info-a.json"),
            transferB = Json.loadTestJson(TransferInfo.class, "transfer/recording/transfer-info-b.json");

    public RecordingTransferIT() {

    }

    @BeforeEach
    public void setUp() {
        messageHandler = new RecordingTestHandler();
        client1 = new VaspClient(module1);
        client1.setCustomMessageHandler(messageHandler);

        client2 = new VaspClient(module2);
        client2.setCustomMessageHandler(messageHandler);
    }

    @AfterEach
    @SneakyThrows
    public void tearDown() {
        client1.close();
        client2.close();
    }

    @Test
    @SneakyThrows
    public void checkTransfer() {
        // Initiate transfer client1 => client2
        var originatorSession = client1.createOriginatorSession(transferA);
        originatorSession.startTransfer();

        // Wait for the transfer to be completed
        assertThat(client1.waitForNoActiveSessions(20, TimeUnit.SECONDS)).isTrue();
        assertThat(client2.waitForNoActiveSessions(5, TimeUnit.SECONDS)).isTrue();

        checkTransfer(
                messageHandler.transferLog,
                TestConstants.VASP_CODE_1,
                TestConstants.VASP_CODE_2,
                transferA);

        // Clear transfer log before the second transfer
        messageHandler.clearTransferLog();

        // Initiate transfer client2 => client1
        originatorSession = client2.createOriginatorSession(transferB);
        originatorSession.startTransfer();

        // Wait for the transfer to be completed
        assertThat(client2.waitForNoActiveSessions(20, TimeUnit.SECONDS)).isTrue();
        assertThat(client1.waitForNoActiveSessions(5, TimeUnit.SECONDS)).isTrue();

        checkTransfer(
                messageHandler.transferLog,
                TestConstants.VASP_CODE_2,
                TestConstants.VASP_CODE_1,
                transferB);
    }

    private void checkTransfer(
            final List<LogRecord> transferLog,
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

}
