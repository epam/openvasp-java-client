package org.openvasp.transfer.recording;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openvasp.client.config.LocalTestModule;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Tag("transfer")
public class LocalRecordingTransferIT extends BaseRecordingTransferIT {

    public LocalRecordingTransferIT() {
        super(LocalTestModule.module1, LocalTestModule.module2);
    }

    @Test
    @Override
    public void checkCallbackStyleTransfer() {
        super.checkCallbackStyleTransfer();
    }

    @Test
    public void checkWaitingStyleTransfer() {
        super.checkWaitingStyleTransfer();
    }

}
