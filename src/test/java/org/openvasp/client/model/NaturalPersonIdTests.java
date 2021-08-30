package org.openvasp.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openvasp.client.common.VaspValidationException;

import java.util.NoSuchElementException;

public class NaturalPersonIdTests {

    @Test
    public void missingIdInformationTest() {
        NaturalPersonId naturalPersonId =new NaturalPersonId();
        VaspMessage vaspMessage = new SessionRequest();
        Assertions.assertThrows(VaspValidationException.class, () -> naturalPersonId.validate(vaspMessage));
    }

    @Test
    public void invalidIdTypeTest() {
        Assertions.assertThrows(NoSuchElementException.class, () -> NaturalPersonId.NatIdType.fromId(-1));
    }
}
