package org.openvasp.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SessionMessageTests {

    @Test
    public void getSenderVaspCodeTest() {
        SessionMessage sessionMessage = new SessionRequest();
        VaspInfo vaspInfo = new VaspInfo();
        vaspInfo.setVaspId(new EthAddr("0x6befaf0656b953b188a0ee3bf3db03d07dface61"));
        sessionMessage.setVaspInfo(vaspInfo);
        Assertions.assertEquals(new VaspCode("7dface61"), sessionMessage.getSenderVaspCode());
    }
}
