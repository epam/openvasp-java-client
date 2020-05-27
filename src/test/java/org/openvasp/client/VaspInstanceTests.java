package org.openvasp.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public class VaspInstanceTests {

    @Test
    public void checkVersion() {
        Assertions.assertEquals("0.0.1", VaspInstance.VERSION);
    }

}
