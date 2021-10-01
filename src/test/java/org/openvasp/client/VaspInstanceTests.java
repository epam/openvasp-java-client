package org.openvasp.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openvasp.client.config.VaspConfig;
import org.openvasp.client.config.VaspModule;
import org.openvasp.client.model.EthAddr;
import org.openvasp.client.model.VaspCode;
import org.openvasp.client.model.VaspInfo;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@ExtendWith(MockitoExtension.class)
public class VaspInstanceTests {

    private VaspModule vaspModuleSpy;
    private VaspInstance vaspInstance;
    private VaspConfig vaspConfig;

    @BeforeEach
    public void init() {
        VaspModule vaspModule = createVaspModule();
        vaspModuleSpy = spy(vaspModule);
        vaspInstance = new VaspInstance(vaspModuleSpy, false);
    }

    @Test
    public void checkVersion() {
        Assertions.assertEquals("0.0.1", VaspInstance.VERSION);
    }

    @Test
    public void closeAndShutdownTest() throws Exception {
        vaspInstance.close();
        vaspInstance.shutdown();
        verify(vaspModuleSpy, times(1)).close();
    }

    public VaspModule createVaspModule() {
        vaspConfig = new VaspConfig();
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
