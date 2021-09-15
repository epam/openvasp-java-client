package org.openvasp.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openvasp.client.common.VaspValidationException;

import java.time.LocalDate;

public class BirthInfoTests {

    private VaspMessage vaspMessage;
    private BirthInfo birthInfo;

    @BeforeEach
    public void init() {
        vaspMessage = new SessionRequest();
        birthInfo = new BirthInfo();
    }

    @Test
    public void missingBirthDateShouldFail() {
        birthInfo.setBirthCity("Zurich");
        birthInfo.setBirthCountry(Country.fromCode("CH"));
        VaspValidationException exception = Assertions.assertThrows(VaspValidationException.class, () -> birthInfo.validate(vaspMessage));
        Assertions.assertEquals("Birth date must be present", exception.getMessage());
    }

    @Test
    public void missingBirthCityShouldFail() {
        birthInfo.setBirthDate(LocalDate.of(2000, 1, 1));
        birthInfo.setBirthCountry(Country.fromCode("CH"));
        VaspValidationException exception = Assertions.assertThrows(VaspValidationException.class, () -> birthInfo.validate(vaspMessage));
        Assertions.assertEquals("Birth city must be present", exception.getMessage());
    }

    @Test
    public void missingBirthCountryShouldFail() {
        birthInfo.setBirthDate(LocalDate.of(2000, 1, 1));
        birthInfo.setBirthCity("Zurich");
        VaspValidationException exception = Assertions.assertThrows(VaspValidationException.class, () -> birthInfo.validate(vaspMessage));
        Assertions.assertEquals("Birth country must be present", exception.getMessage());
    }
}
