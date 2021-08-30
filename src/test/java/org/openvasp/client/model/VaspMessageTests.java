package org.openvasp.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openvasp.client.common.VaspException;
import org.openvasp.client.common.VaspValidationException;

import java.util.NoSuchElementException;

public class VaspMessageTests {

    private VaspMessage vaspMessage;

    @BeforeEach
    public void init() {
        vaspMessage = new SessionRequest();
    }

    @Test
    public void downcastTest() {
        vaspMessage.asSessionRequest();
        vaspMessage = new SessionReply();
        vaspMessage.asSessionReply();
        vaspMessage = new TransferRequest();
        vaspMessage.asTransferRequest();
        vaspMessage = new TransferReply();
        vaspMessage.asTransferReply();
        vaspMessage = new TransferDispatch();
        vaspMessage.asTransferDispatch();
        vaspMessage = new TransferConfirmation();
        vaspMessage.asTransferConfirmation();
        vaspMessage = new TerminationMessage();
        vaspMessage.asTerminationMessage();
        VaspMessage finalVaspMessage = vaspMessage;
        Assertions.assertThrows(VaspException.class, finalVaspMessage::asTransferConfirmation);
    }

    @Test
    public void invalidMessageIdTest() {
        VaspMessage.Header header = new VaspMessage.Header();
        header.setMessageType(VaspMessage.TypeDescriptor.SESSION_REQUEST);
        header.setMessageId("eaae0fcbf6a342aec65936ea208653");
        header.setSessionId("0xfe3f216d0de7f94ba978225842c7330c");
        header.setResponseCode("1");
        vaspMessage.setHeader(header);
        Assertions.assertThrows(VaspValidationException.class, () -> vaspMessage.validate());
    }

    @Test
    public void invalidSessionIdTest() {
        VaspMessage.Header header = new VaspMessage.Header();
        header.setMessageType(VaspMessage.TypeDescriptor.SESSION_REQUEST);
        header.setMessageId("0x32eaae0fcbf6a342aec65936ea208653");
        header.setSessionId("fe3f216d0de7f94ba978225842c733");
        header.setResponseCode("1");
        vaspMessage.setHeader(header);
        Assertions.assertThrows(VaspValidationException.class, () -> vaspMessage.validate());
    }

    @Test
    public void invalidTypeDescriptorTest() {
        Assertions.assertThrows(NoSuchElementException.class, () -> VaspMessage.TypeDescriptor.fromIdStr("-1"));
    }

    @Test
    public void getConfirmationTopicTest() {
        VaspMessage.Header header = new VaspMessage.Header();
        header.setMessageId("0x32eaae0fcbf6a342aec65936ea208653");
        vaspMessage.setHeader(header);
        Assertions.assertEquals(new Topic("0xea208653"), vaspMessage.getConfirmationTopic());
    }
}
