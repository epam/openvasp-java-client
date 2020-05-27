package org.openvasp.transfer.account;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openvasp.client.config.RopstenTestModule;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Tag("transfer")
@Tag("ropsten")
public class RopstenAccountTransferIT extends BaseAccountTransferIT {

    public RopstenAccountTransferIT() {
        super(RopstenTestModule.module1, RopstenTestModule.module2, RopstenTestModule.module3);
    }

    @Test
    public void checkMultipleTransfers() {
        super.checkMultipleTransfers();
    }

}
