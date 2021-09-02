package org.openvasp.client.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openvasp.client.model.EthAddr;
import org.openvasp.client.model.SessionRequest;
import org.openvasp.client.model.VaspMessage;

import java.util.Optional;

public class VaspIdentityServiceImplTests {

    private VaspIdentityServiceImpl vaspIdentityService;
    private VaspMessage vaspMessage;

    @BeforeEach
    public void init() {
        vaspIdentityService = new VaspIdentityServiceImpl();
        vaspMessage = new SessionRequest();
        VaspMessage.Header header = new VaspMessage.Header();
        header.setMessageId("0x32eaae0fcbf6a342aec65936ea208653");
        header.setSessionId("0xfe3f216d0de7f94ba978225842c7330c");
        header.setMessageType(VaspMessage.TypeDescriptor.SESSION_REQUEST);
        header.setResponseCode("1");
        vaspMessage.setHeader(header);
    }

    @Test
    public void resolveSenderVaspIdTest() {
        String ethAddress = "0x08fda931d64b17c3acffb35c1b3902e0bbb4ee5c";
        EthAddr contractAddress = new EthAddr(ethAddress);
        vaspIdentityService.addVaspIdResolver((message -> Optional.of(contractAddress)));
        Assertions.assertEquals(Optional.of(new EthAddr(ethAddress)), vaspIdentityService.resolveSenderVaspId(vaspMessage));
    }

    @Test
    public void resolveSenderVaspIdEmptyTest() {
        vaspIdentityService.addVaspIdResolver((message -> Optional.empty()));
        Assertions.assertEquals(Optional.empty(), vaspIdentityService.resolveSenderVaspId(vaspMessage));
    }
}
