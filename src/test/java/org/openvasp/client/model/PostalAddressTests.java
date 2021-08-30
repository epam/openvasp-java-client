package org.openvasp.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openvasp.client.common.VaspValidationException;

public class PostalAddressTests {

    private VaspMessage vaspMessage;

    @BeforeEach
    public void init() {
        vaspMessage = new SessionRequest();
    }

    @Test
    public void missingPostCodeTest() {
        PostalAddress postalAddress = new PostalAddress("Boulevard Lilienthal", "2", "Boulevard Lilienthal 2", "", "Opfikon", Country.fromCode("CH"));
        Assertions.assertThrows(VaspValidationException.class, () -> postalAddress.validate(vaspMessage));
    }

    @Test
    public void missingTownTest() {
        PostalAddress postalAddress = new PostalAddress("Boulevard Lilienthal", "2", "Boulevard Lilienthal 2", "8152", "", Country.fromCode("CH"));
        Assertions.assertThrows(VaspValidationException.class, () -> postalAddress.validate(vaspMessage));
    }

    @Test
    public void missingCountryTest() {
        PostalAddress postalAddress = new PostalAddress("Boulevard Lilienthal", "2", "Boulevard Lilienthal 2", "8152", "Opfikon", null);
        Assertions.assertThrows(VaspValidationException.class, () -> postalAddress.validate(vaspMessage));
    }

    @Test
    public void missingAddressLineTest() {
        PostalAddress postalAddress = new PostalAddress("Boulevard Lilienthal", "2", "", "8152", "Opfikon", null);
        Assertions.assertThrows(VaspValidationException.class, () -> postalAddress.validate(vaspMessage));
    }
}
