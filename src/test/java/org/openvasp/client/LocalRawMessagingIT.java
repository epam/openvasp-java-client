package org.openvasp.client;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openvasp.client.config.LocalTestModule;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Tag("raw-messaging")
public class LocalRawMessagingIT extends BaseRawMessagingIT {

    public LocalRawMessagingIT() {
        super(LocalTestModule.module1, LocalTestModule.module2);
    }

    @Test
    @Override
    public void checkAsymmetricSendAndReceive() {
        super.checkAsymmetricSendAndReceive();
    }

    @Test
    @Override
    public void checkSymmetricSendAndReceive() {
        super.checkSymmetricSendAndReceive();
    }

}
