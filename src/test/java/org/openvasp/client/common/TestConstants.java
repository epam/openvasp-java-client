package org.openvasp.client.common;

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
    String CONTRACT_ADDRESS_1 = "0x6befaf0656b953b188a0ee3bf3db03d07dface61";

    // Juridical smart contract address
    String CONTRACT_ADDRESS_2 = "0x08fda931d64b17c3acffb35c1b3902e0bbb4ee5c";

    // Bank smart contract address
    String CONTRACT_ADDRESS_3 = "0x4dd7e1e2d5640a06ed81f155f171012f1cd48daa";

    VaspCode VASP_CODE_1 = new VaspCode("7dface61");
    VaspCode VASP_CODE_2 = new VaspCode("bbb4ee5c");
    VaspCode VASP_CODE_3 = new VaspCode("1cd48daa");

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

    String[] VAAN_1_LIST = {
            "7dface6100000000000001a7",
            "7dface6100000000000002a8",
            "7dface6100000000000003a9"
    };

    String[] VAAN_2_LIST = {
            "bbb4ee5c00000000000005be",
            "bbb4ee5c00000000000006bf",
            "bbb4ee5c00000000000007c0"
    };

    String[] VAAN_3_LIST = {
            "1cd48daa0000000000000a31",
            "1cd48daa0000000000000b32",
            "1cd48daa0000000000000c33"
    };

    long WAIT_TIMEOUT_1 = 20000;
    long WAIT_TIMEOUT_2 = 5000;

}
