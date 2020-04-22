package org.openvasp.transfer.recording;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.openvasp.client.SimpleTransferHandler;
import org.openvasp.client.model.VaspCode;
import org.openvasp.client.model.VaspMessage;
import org.openvasp.client.session.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Slf4j
final class RecordingTestHandler implements SimpleTransferHandler {

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
        SimpleTransferHandler.super.accept(message, session);
    }

    void logRecord(final VaspCode vaspCode, final VaspMessage vaspMessage) {
        transferLog.add(new TransferLogRecord(vaspCode, vaspMessage));
        log.debug("process {} at {}", vaspMessage.getClass().getSimpleName(), vaspCode);
    }

    void clearTransferLog() {
        transferLog.clear();
    }

}
