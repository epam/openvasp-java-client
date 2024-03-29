package org.openvasp.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openvasp.client.common.VaspValidationException;

import java.util.NoSuchElementException;

public class JuridicalPersonIdTests {

    @Test
    public void validationTest() {
        JuridicalPersonId juridicalPersonId = new JuridicalPersonId();
        VaspMessage vaspMessage = new SessionRequest();
        Assertions.assertThrows(VaspValidationException.class, () -> juridicalPersonId.validate(vaspMessage));

        juridicalPersonId.setJurIdCountry(Country.fromCode("CH"));
        juridicalPersonId.setJurId("123456AH");
        juridicalPersonId.setJurIdIssuer("ID");
        juridicalPersonId.validate(vaspMessage);
        Assertions.assertDoesNotThrow(() -> juridicalPersonId.validate(vaspMessage));
    }

    @Test
    public void invalidIdTypeTest() {
        Assertions.assertThrows(NoSuchElementException.class, () -> JuridicalPersonId.JurIdType.fromId(-1));
    }
}
