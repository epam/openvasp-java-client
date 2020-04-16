package org.openvasp.client.service.impl;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.util.encoders.Hex;
import org.openvasp.client.common.annotation.ContractNode;
import org.openvasp.client.contract.VASP;
import org.openvasp.client.model.Country;
import org.openvasp.client.model.PostalAddress;
import org.openvasp.client.model.VaspCode;
import org.openvasp.client.model.VaspContractInfo;
import org.openvasp.client.service.ContractService;
import org.openvasp.client.service.EnsService;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.DefaultGasProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jan_Juraszek@epam.com
 * @author Olexandr_Bilovol@epam.com
 */
@Singleton
@Slf4j
public final class ContractServiceImpl implements ContractService {

    private final Web3j web3j;
    private final EnsService ensService;
    private final Map<String, VaspContractInfo> cache = new ConcurrentHashMap<>();

    @Inject
    public ContractServiceImpl(@ContractNode final Web3j web3j, final EnsService ensService) {
        this.web3j = web3j;
        this.ensService = ensService;
    }

    @Override
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public VaspContractInfo getVaspContractInfo(@NonNull final String vaspSmartContracAddress) {
        VaspContractInfo result = cache.get(vaspSmartContracAddress);
        if (result != null) {
            return result;
        }

        log.debug("Request for the contract info at the address {}", vaspSmartContracAddress);

        val contract = VASP.load(vaspSmartContracAddress, web3j, Credentials.create("0x0"), new DefaultGasProvider());

        val postalAddress = contract.postalAddress().send();
        val address = PostalAddress.builder()
                .street(postalAddress.component1())
                .number(postalAddress.component2())
                .adrline(postalAddress.component3())
                .postCode(postalAddress.component4())
                .town(postalAddress.component5())
                .country(Country.ALL.get(postalAddress.component6()))
                .build();

        result = VaspContractInfo.builder()
                .name(contract.name().send())
                .vaspCode(new VaspCode(Hex.toHexString(contract.code().send())))
                .channels((List<Long>) contract.channels().send())
                .handshakeKey(contract.handshakeKey().send())
                .signingKey(contract.signingKey().send())
                .ownerAddress(contract.owner().send())
                .email(contract.email().send())
                .website(contract.website().send())
                .address(address)
                .build();
        cache.putIfAbsent(vaspSmartContracAddress, result);

        log.debug("The contract information received, VASP code = {}", result.getVaspCode());

        return result;
    }

    @Override
    public VaspContractInfo getVaspContractInfo(@NonNull final VaspCode vaspCode) {
        // See section 5.4 of OpenVasp_Whitepaper.pdf for an example
        // of conversion a VASP code to the corresponding ENS name
        val ensContractId = vaspCode + ".eth";
        return getVaspContractInfo(ensService.resolveContractAddress(ensContractId));
    }

}
