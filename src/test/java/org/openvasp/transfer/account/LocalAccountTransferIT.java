package org.openvasp.transfer.account;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openvasp.client.config.LocalTestModule;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Tag("transfer")
public class LocalAccountTransferIT extends BaseAccountTransferIT {

    public LocalAccountTransferIT() {
        super(LocalTestModule.module1, LocalTestModule.module2, LocalTestModule.module3);
    }

    @Test
    public void checkMultipleTransfers() {
        super.checkMultipleTransfers();
    }

}
