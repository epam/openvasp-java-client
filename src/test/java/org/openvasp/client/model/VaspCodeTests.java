package org.openvasp.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VaspCodeTests {

    @Test
    public void toTopicTest() {
        VaspCode vaspCode = new VaspCode("12345678");
        Assertions.assertEquals(new Topic("0x12345678"), vaspCode.toTopic());
    }
}
