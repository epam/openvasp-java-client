package org.openvasp.client.service.impl;

import com.google.common.collect.ImmutableMap;
import org.openvasp.client.common.VaspException;
import org.openvasp.client.service.EnsService;

import javax.inject.Singleton;
import java.util.Map;

import static org.openvasp.client.common.TestConstants.*;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Singleton
public final class EnsServiceMock implements EnsService {

    private final Map<String, String> ensMap;

    public EnsServiceMock() {
        this.ensMap = ImmutableMap.<String, String>builder()
                .put(CONTRACT_ID_1, CONTRACT_ADDRESS_1)
                .put(CONTRACT_ID_2, CONTRACT_ADDRESS_2)
                .put(CONTRACT_ID_3, CONTRACT_ADDRESS_3)
                .build();
    }

    @Override
    public String resolveContractAddress(String contractId) {
        if (!ensMap.containsKey(contractId)) {
            throw new VaspException("Contract for the VASP code '%s' was not found", contractId);
        }
        return ensMap.get(contractId);
    }

}
