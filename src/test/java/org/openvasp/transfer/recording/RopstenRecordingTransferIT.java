package org.openvasp.transfer.recording;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openvasp.client.config.RopstenTestModule;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Tag("transfer")
@Tag("ropsten")
public class RopstenRecordingTransferIT extends BaseRecordingTransferIT {

    public RopstenRecordingTransferIT() {
        super(RopstenTestModule.module1, RopstenTestModule.module2);
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
