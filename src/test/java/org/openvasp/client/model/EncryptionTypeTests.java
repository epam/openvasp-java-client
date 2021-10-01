package org.openvasp.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

public class EncryptionTypeTests {

    @Test
    public void invalidEncryptionTypeTest() {
        Assertions.assertThrows(NoSuchElementException.class, () -> EncryptionType.fromId(-1));
    }

    @Test
    public void validEncryptionTypeTest() {
        Assertions.assertEquals(EncryptionType.ASSYMETRIC, EncryptionType.fromId(1));
    }
}
