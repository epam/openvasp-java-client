package org.openvasp.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openvasp.client.common.VaspValidationException;

public class OriginatorTests {

    private Originator originator;
    private VaspMessage vaspMessage;

    @BeforeEach
    public void init() {
        originator = new Originator();
        vaspMessage = new SessionRequest();
    }

    @Test
    public void missingNameTest() {
        originator.setVaan(new Vaan("10007dface61fb0828095d55"));
        originator.setAddress(new PostalAddress());
        Assertions.assertThrows(VaspValidationException.class, () -> originator.validate(vaspMessage));
    }

    @Test
    public void missingVaanTest() {
        originator.setName("Average Joe");
        originator.setAddress(new PostalAddress());
        Assertions.assertThrows(VaspValidationException.class, () -> originator.validate(vaspMessage));
    }

    @Test
    public void missingAddressTest() {
        originator.setName("Average Joe");
        originator.setVaan(new Vaan("10007dface61fb0828095d55"));
        Assertions.assertThrows(VaspValidationException.class, () -> originator.validate(vaspMessage));
    }
}
