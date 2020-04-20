package org.openvasp.client.messaging;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openvasp.client.config.RopstenTestModule;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Tag("raw-messaging")
public class RopstenRawMessagingIT extends BaseRawMessagingIT {

    public RopstenRawMessagingIT() {
        super(RopstenTestModule.module1, RopstenTestModule.module2);
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
