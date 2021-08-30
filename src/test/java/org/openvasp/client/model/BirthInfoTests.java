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
    public void missingBirthDateTest() {
        birthInfo.setBirthCity("Zurich");
        birthInfo.setBirthCountry(Country.fromCode("CH"));
        Assertions.assertThrows(VaspValidationException.class, () -> birthInfo.validate(vaspMessage));
    }

    @Test
    public void missingBirthCityTest() {
        birthInfo.setBirthDate(LocalDate.of(2000, 1, 1));
        birthInfo.setBirthCountry(Country.fromCode("CH"));
        Assertions.assertThrows(VaspValidationException.class, () -> birthInfo.validate(vaspMessage));
    }

    @Test
    public void missingBirthCountryTest() {
        birthInfo.setBirthDate(LocalDate.of(2000, 1, 1));
        birthInfo.setBirthCity("Zurich");
        Assertions.assertThrows(VaspValidationException.class, () -> birthInfo.validate(vaspMessage));
    }
}
