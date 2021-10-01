package org.openvasp.client.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openvasp.client.common.VaspValidationException;

public class SessionRequestTests {


    @Test
    public void invalidSessionPublicKeyTest() {
        SessionRequest sessionRequest = new SessionRequest();
        sessionRequest.setHandshake(new SessionRequest.Handshake(new Topic("0x527aeb21"),
                "ac7c"));
        Assertions.assertThrows(VaspValidationException.class, sessionRequest::getSessionPublicKey);
        Assertions.assertThrows(VaspValidationException.class, sessionRequest::validate);
    }

    @Test
    public void getSessionPublicKeyTest() {
        SessionRequest sessionRequest = new SessionRequest();
        Topic topic = new Topic("0x527aeb21");
        String ecdhpk = "ac7c9764497e5e5d3c42a4cc0b1425fda9f7564e49f288994fdbbb42e5731bb0f30f82e1890459cb40325989a6a872a963d40efdbc88d66e427da1ecded0e4c2";
        sessionRequest.setHandshake(new SessionRequest.Handshake(topic, "0x" + ecdhpk));
        Assertions.assertEquals("0x04" + ecdhpk, sessionRequest.getSessionPublicKey());
    }
}
