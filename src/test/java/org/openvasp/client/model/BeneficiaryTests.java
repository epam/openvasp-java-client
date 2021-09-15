package org.openvasp.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openvasp.client.common.VaspValidationException;

public class BeneficiaryTests {

    @Test
    public void beneficiaryWithNoNameShouldFail() {
        Beneficiary beneficiary = new Beneficiary("", new Vaan("10007dface61fb0828095d55"));
        VaspMessage vaspMessage = new SessionRequest();
        VaspValidationException exception = Assertions.assertThrows(VaspValidationException.class, () -> beneficiary.validate(vaspMessage));
        Assertions.assertEquals("Beneficiary name must be present", exception.getMessage());
    }
}
