package org.openvasp.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Olexandr_Bilovol@epam.com
 */
class VaspClientTests {

    @Test
    void checkVersion() {
        Assertions.assertEquals("0.0.1", VaspClient.VERSION);
    }

}
