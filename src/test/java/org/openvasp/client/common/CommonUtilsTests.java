package org.openvasp.client.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openvasp.client.model.Originator;
import org.openvasp.client.model.SessionRequest;
import org.openvasp.client.model.VaspMessage;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CommonUtilsTests {

    @Test
    public void toHexTest() {
        byte[] array = "test_string".getBytes();
        Assertions.assertEquals("746573745f737472696e67", VaspUtils.toHex(array));
    }

    @Test
    public void hexStrEncodeTest() {
        Assertions.assertEquals("746573745f737472696e67", VaspUtils.hexStrEncode("test_string"));
    }

    @Test
    public void tuple2Test() {
        Tuple2<String, String> tuple = Tuple2.of("A", "B");
        Assertions.assertEquals("A", tuple._1());
        Assertions.assertEquals("A", tuple.first());
        Assertions.assertEquals("B", tuple._2());
        Assertions.assertEquals("B", tuple.second());
        Assertions.assertEquals("(A, B)", tuple.toString());
    }

    @Test
    public void tuple3Test() {
        Tuple3<String, String, String> tuple = Tuple3.of("A", "B", "C");
        Assertions.assertEquals("A", tuple._1());
        Assertions.assertEquals("B", tuple._2());
        Assertions.assertEquals("C", tuple._3());
        Assertions.assertEquals("(A, B, C)", tuple.toString());
    }

    @Test
    public void toHexJsonTest() {
        Assertions.assertEquals("0x22746573745f737472696e6722", Json.toHexJson("test_string"));
    }

    @Test
    public void fromHexJsonTest() {
        Assertions.assertEquals("test_string", Json.fromHexJson(String.class, "0x22746573745f737472696e6722"));
    }

    @Test
    public void readJsonPathTest() {
        String resourcePath = "serialization/beneficiary-1.json";
        String jsonPath = "$['name']";
        String result = Json.readJsonPath(resourcePath, jsonPath);
        Assertions.assertEquals("John Smith", result);
    }

    @Test
    public void loadFileJsonTest() {
        String resourcePath = "src/test/resources/json-test-data/serialization/originator.json";
        Originator originator = Json.loadFileJson(Originator.class, resourcePath);
        Assertions.assertEquals("John Smith", originator.getName());
    }

    @Test
    public void loadFileYamlTest() {
        String resourcePath = "src/test/resources/json-test-data/serialization/originator.yaml";
        Originator originator = Json.loadFileYaml(Originator.class, resourcePath);
        Assertions.assertEquals("John Smith", originator.getName());
    }

    @Test
    public void vaspValidationExceptionTest() {
        VaspMessage message = createVaspMessage();
        VaspValidationException exception = new VaspValidationException(message);
        Assertions.assertEquals("1", exception.getSource().getHeader().getResponseCode());
        exception = new VaspValidationException(message, new Throwable());
        Assertions.assertEquals("1", exception.getSource().getHeader().getResponseCode());
        exception = new VaspValidationException(message, "exception thrown", new Throwable());
        Assertions.assertEquals("exception thrown", exception.getMessage());
    }

    @Test
    public void exceptionHandlerDelegateTest() {
        VaspMessage message = createVaspMessage();
        VaspValidationException exception = new VaspValidationException(message);
        ExceptionHandlerDelegate main = spy(ExceptionHandlerDelegate.class);
        ExceptionHandlerDelegate delegate = new ExceptionHandlerDelegate();
        main.setDelegate(delegate);
        main.processException(exception);
        verify(main).processException(exception);
    }

    private VaspMessage createVaspMessage() {
        VaspMessage message = new SessionRequest();
        VaspMessage.Header header = new VaspMessage.Header();
        header.setResponseCode("1");
        message.setHeader(header);
        return message;
    }
}
