package org.openvasp.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

public class CountryTests {

    @Test
    public void invalidCountryTest() {
        Assertions.assertThrows(NoSuchElementException.class, () -> Country.fromCode("-1"));
    }
}
