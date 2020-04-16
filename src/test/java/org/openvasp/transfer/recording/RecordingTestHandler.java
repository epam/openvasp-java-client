package org.openvasp.transfer.recording;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.openvasp.client.model.*;
import org.openvasp.client.session.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import static org.openvasp.client.model.VaspResponseCode.OK;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Slf4j
final class RecordingTestHandler implements BiConsumer<VaspMessage, Session> {

    final List<TransferLogRecord> transferLog;

    public RecordingTestHandler(final List<TransferLogRecord> transferLog) {
        if (transferLog != null) {
            this.transferLog = transferLog;
        } else {
            this.transferLog = Collections.synchronizedList(new ArrayList<>());
        }
    }

    public RecordingTestHandler() {
        this(null);
    }

    @Override
    public void accept(@NonNull final VaspMessage message, @NonNull final Session session) {
        logRecord(session.vaspCode(), message);

        if (message instanceof SessionRequest) {
            val response = new SessionReply();
            response.getHeader().setResponseCode(OK.id);
            session.sendMessage(response);
        }

        if (message instanceof SessionReply) {
            val response = new TransferRequest();
            response.getHeader().setResponseCode(OK.id);
            response.setOriginator(session.transferInfo().getOriginator());
            response.setBeneficiary(session.transferInfo().getBeneficiary());
            response.setTransfer(session.transferInfo().getTransfer());
            session.sendMessage(response);
        }

        if (message instanceof TransferRequest) {
            val request = (TransferRequest) message;
            val response = new TransferReply();
            response.getHeader().setResponseCode(OK.id);
            response.setOriginator(request.getOriginator());
            response.setBeneficiary(request.getBeneficiary());
            response.setTransfer(request.getTransfer());
            session.sendMessage(response);
        }

        if (message instanceof TransferReply) {
            val response = new TransferDispatch();
            response.getHeader().setResponseCode(OK.id);
            response.setOriginator(session.transferInfo().getOriginator());
            response.setBeneficiary(session.transferInfo().getBeneficiary());
            response.setTransfer(session.transferInfo().getTransfer());
            session.sendMessage(response);
        }

        if (message instanceof TransferDispatch) {
            val response = new TransferConfirmation();
            response.getHeader().setResponseCode(OK.id);
            response.setOriginator(session.transferInfo().getOriginator());
            response.setBeneficiary(session.transferInfo().getBeneficiary());
            response.setTransfer(session.transferInfo().getTransfer());
            response.setTx(session.transferInfo().getTx());
            session.sendMessage(response);
        }

        if (message instanceof TransferConfirmation) {
            val response = new TerminationMessage();
            response.getHeader().setResponseCode(OK.id);
            session.sendMessage(response);
            session.remove();
        }

        if (message instanceof TerminationMessage) {
            session.remove();
        }
    }

    void logRecord(final VaspCode vaspCode, final VaspMessage vaspMessage) {
        transferLog.add(new TransferLogRecord(vaspCode, vaspMessage));
        log.debug("process {} at {}", vaspMessage.getClass().getSimpleName(), vaspCode);
    }

    void clearTransferLog() {
        transferLog.clear();
    }

}
