package org.openvasp.client.common;

import org.openvasp.client.model.EthAddr;
import org.openvasp.client.model.Vaan;
import org.openvasp.client.model.VaspCode;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public interface TestConstants {

    /*
        Suffix
            _1 for Person smart contract
            _2 for Juridical smart contract
            _3 for Bank smart contract
        See OpenVASP.Tests.Client.ClientExampleTest for details
     */

    // Person smart contract address
    EthAddr CONTRACT_ADDRESS_1 = new EthAddr("0x6befaf0656b953b188a0ee3bf3db03d07dface61");

    // Juridical smart contract address
    EthAddr CONTRACT_ADDRESS_2 = new EthAddr("0x08fda931d64b17c3acffb35c1b3902e0bbb4ee5c");

    // Bank smart contract address
    EthAddr CONTRACT_ADDRESS_3 = new EthAddr("0x4dd7e1e2d5640a06ed81f155f171012f1cd48daa");

    VaspCode VASP_CODE_1 = CONTRACT_ADDRESS_1.toVaspCode();
    VaspCode VASP_CODE_2 = CONTRACT_ADDRESS_2.toVaspCode();
    VaspCode VASP_CODE_3 = CONTRACT_ADDRESS_3.toVaspCode();

    String CONTRACT_ID_1 = "7dface61.eth";
    String CONTRACT_ID_2 = "bbb4ee5c.eth";
    String CONTRACT_ID_3 = "1cd48daa.eth";

    String VASP_CONFIG_LOCAL_1 = "vasp-config/local/vasp-config-1.json";
    String VASP_CONFIG_LOCAL_2 = "vasp-config/local/vasp-config-2.json";
    String VASP_CONFIG_LOCAL_3 = "vasp-config/local/vasp-config-3.json";

    String VASP_CONFIG_ROPSTEN_1 = "vasp-config/ropsten/vasp-config-1.json";
    String VASP_CONFIG_ROPSTEN_2 = "vasp-config/ropsten/vasp-config-2.json";
    String VASP_CONFIG_ROPSTEN_3 = "vasp-config/ropsten/vasp-config-3.json";

    String VASP_CONTRACT_LOCAL_1 = "vasp-config/local/vasp-contract-1.json";
    String VASP_CONTRACT_LOCAL_2 = "vasp-config/local/vasp-contract-2.json";
    String VASP_CONTRACT_LOCAL_3 = "vasp-config/local/vasp-contract-3.json";

    Vaan[] VAAN_1_LIST = {
            new Vaan("7dface6100000000000001a7"),
            new Vaan("7dface6100000000000002a8"),
            new Vaan("7dface6100000000000003a9")
    };

    Vaan[] VAAN_2_LIST = {
            new Vaan("bbb4ee5c00000000000005be"),
            new Vaan("bbb4ee5c00000000000006bf"),
            new Vaan("bbb4ee5c00000000000007c0")
    };

    Vaan[] VAAN_3_LIST = {
            new Vaan("1cd48daa0000000000000a31"),
            new Vaan("1cd48daa0000000000000b32"),
            new Vaan("1cd48daa0000000000000c33")
    };

    long WAIT_TIMEOUT_1 = 25000;
    long WAIT_TIMEOUT_2 = 10000;
    long WAIT_TIMEOUT_3 = 3000;

}
