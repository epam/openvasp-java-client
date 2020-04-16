package org.openvasp.client.config;

import org.openvasp.client.common.Json;
import org.openvasp.client.service.ContractService;
import org.openvasp.client.service.impl.ContractServiceMock;

import static org.openvasp.client.common.TestConstants.*;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public class LocalTestModule extends VaspModule {

    public static final LocalTestModule module1;
    public static final LocalTestModule module2;
    public static final LocalTestModule module3;

    static {
        module1 = new LocalTestModule(Json.loadTestJson(VaspConfig.class, VASP_CONFIG_LOCAL_1));
        module2 = new LocalTestModule(Json.loadTestJson(VaspConfig.class, VASP_CONFIG_LOCAL_2));
        module3 = new LocalTestModule(Json.loadTestJson(VaspConfig.class, VASP_CONFIG_LOCAL_3));
    }

    public LocalTestModule(final VaspConfig vaspConfig) {
        super(vaspConfig);
    }

    @Override
    protected void bindContractService() {
        bind(ContractService.class).to(ContractServiceMock.class);
    }

}
