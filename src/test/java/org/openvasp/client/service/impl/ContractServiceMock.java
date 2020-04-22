package org.openvasp.client.service.impl;

import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.openvasp.client.common.Json;
import org.openvasp.client.common.VaspException;
import org.openvasp.client.model.EthAddr;
import org.openvasp.client.model.VaspCode;
import org.openvasp.client.model.VaspContractInfo;
import org.openvasp.client.service.ContractService;

import javax.inject.Singleton;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openvasp.client.common.TestConstants.*;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Singleton
public final class ContractServiceMock implements ContractService {

    private final Map<EthAddr, VaspContractInfo> contracts;
    private final Map<VaspCode, VaspContractInfo> ensMap;

    public ContractServiceMock() {
        val contract1 = Json.loadTestJson(VaspContractInfo.class, VASP_CONTRACT_LOCAL_1);
        val contract2 = Json.loadTestJson(VaspContractInfo.class, VASP_CONTRACT_LOCAL_2);
        val contract3 = Json.loadTestJson(VaspContractInfo.class, VASP_CONTRACT_LOCAL_3);

        assertThat(contract1.getHandshakeKey()).isNotEmpty();
        assertThat(contract2.getHandshakeKey()).isNotEmpty();
        assertThat(contract3.getHandshakeKey()).isNotEmpty();

        this.contracts = ImmutableMap.<EthAddr, VaspContractInfo>builder()
                .put(CONTRACT_ADDRESS_1, contract1)
                .put(CONTRACT_ADDRESS_2, contract2)
                .put(CONTRACT_ADDRESS_3, contract3)
                .build();

        val ensMapBuilder = ImmutableMap.<VaspCode, VaspContractInfo>builder();
        for (val entry : contracts.entrySet()) {
            ensMapBuilder.put(entry.getKey().toVaspCode(), entry.getValue());
        }
        this.ensMap = ensMapBuilder.build();
    }

    @Override
    @SneakyThrows
    public VaspContractInfo getVaspContractInfo(@NonNull final EthAddr vaspSmartContracAddress) {
        if (!contracts.containsKey(vaspSmartContracAddress)) {
            throw new VaspException("Contract with the address '%s' was not found", vaspSmartContracAddress);
        }
        return contracts.get(vaspSmartContracAddress);
    }

    @Override
    public VaspContractInfo getVaspContractInfo(@NonNull final VaspCode vaspCode) {
        if (!ensMap.containsKey(vaspCode)) {
            throw new VaspException("Contract for the VASP code '%s' was not found", vaspCode);
        }
        return ensMap.get(vaspCode);
    }

}
