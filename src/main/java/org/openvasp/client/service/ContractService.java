package org.openvasp.client.service;

import org.openvasp.client.model.EthAddr;
import org.openvasp.client.model.VaspCode;
import org.openvasp.client.model.VaspContractInfo;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public interface ContractService {

    /**
     * Get information about VASP instance.
     *
     * @param vaspSmartContractAddress Address of Ethereum smart contract
     * @return VaspContractInfo object containing data about the participant
     */
    VaspContractInfo getVaspContractInfo(EthAddr vaspSmartContractAddress);

    /**
     * Get information about VASP instance by VASP code.
     * The implementation should use Ethereum Name Service to make conversion
     * from VASP code to the contract address
     *
     * @param vaspCode VASP code of the VASP instance
     * @return VaspContractInfo object containing data about the participant
     */
    VaspContractInfo getVaspContractInfo(VaspCode vaspCode);

}
