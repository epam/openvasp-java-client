package org.openvasp.client.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openvasp.client.model.EthAddr;
import org.openvasp.client.model.VaspCode;
import org.openvasp.client.model.VaspInfo;

public class VaspModuleTest {

    @Test
    public void getVaspCodeOfVaspModuleTest() {
        VaspModule vaspModule = createVaspModule();
        Assertions.assertEquals(new VaspCode("7dface61"), vaspModule.getVaspCode());
    }

    public VaspModule createVaspModule() {
        VaspConfig vaspConfig = new VaspConfig();
        vaspConfig.setVaspCode(new VaspCode("7dface61"));
        vaspConfig.setHttpServiceUrl("http://serviceurl.org");
        vaspConfig.setHandshakePrivateKey("0x08015803208005d33e3117d63e99b584c1ea011f78c79c6db42f49e568943486");

        VaspInfo vaspInfo = new VaspInfo();
        vaspInfo.setVaspId(new EthAddr("0x6befaf0656b953b188a0ee3bf3db03d07dface61"));
        vaspInfo.setPk("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef");
        vaspConfig.setVaspInfo(vaspInfo);

        return new VaspModule(vaspConfig);
    }
}
