package org.openvasp.client.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openvasp.client.common.Constants;
import org.openvasp.client.common.Json;
import org.openvasp.client.common.VaspUtils;
import org.openvasp.client.model.*;
import org.openvasp.client.service.ContractService;
import org.openvasp.client.service.VaspIdentityService;
import org.skyscreamer.jsonassert.JSONAssert;
import org.web3j.crypto.ECKeyPair;
import org.web3j.utils.Numeric;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
public class SignServiceTests {

    @Mock
    ContractService contractService;
    @Mock
    VaspIdentityService vaspIdentityService;

    @Data
    public static class SignTestItem {
        @JsonProperty("payload")
        String payloadWithSignature;

        @JsonProperty("key")
        String privateSigningKey;

        @JsonProperty("json")
        String jsonStr;

        @JsonProperty("type")
        String messageType;

        String getPayloadHex() {
            return StringUtils.left(payloadWithSignature, payloadWithSignature.length() - Constants.SIGNATURE_LENGTH);
        }

        String getSignatureHex() {
            return StringUtils.right(payloadWithSignature, Constants.SIGNATURE_LENGTH);
        }
    }

    @Getter
    @Setter
    public static class SignTestList {
        @JsonProperty("content")
        List<SignTestItem> content;
    }

    @Test
    @SneakyThrows
    public void checkSignatureCreationAndVerification() {
        val testData = Json.loadTestYaml(SignTestList.class, "signature/signed-messages.yaml");

        for (val testRecord : testData.content) {
            // derive public key from private key and create a mock SignService instance
            val keyPair = ECKeyPair.create(Hex.decode(Numeric.cleanHexPrefix(testRecord.privateSigningKey)));
            val publicSigningKey = Numeric.toHexStringWithPrefix(keyPair.getPublicKey());
            val contractInfo = new VaspContractInfo();
            contractInfo.setSigningKey(publicSigningKey);

            val signService = new SignServiceImpl(
                    new ContractService() {
                        @Override
                        public VaspContractInfo getVaspContractInfo(EthAddr vaspSmartContractAddress) {
                            return getVaspContractInfo(vaspSmartContractAddress.toVaspCode());
                        }

                        @Override
                        public VaspContractInfo getVaspContractInfo(VaspCode vaspCode) {
                            return contractInfo;
                        }
                    },
                    message -> Optional.of(new EthAddr("0x6befaf0656b953b188a0ee3bf3db03d07dface61")));

            // create signature from original payload and verify if it's same as the original signature
            val recalculatedSignature = signService.signPayload(
                    testRecord.getPayloadHex(),
                    testRecord.privateSigningKey);
            assertThat(recalculatedSignature).isEqualTo(testRecord.getSignatureHex());

            // verify original signature against original payload and, if correct, validate and extract the message
            val vaspMsg = signService.extractSignedMessage(testRecord.payloadWithSignature);

            val originalJson = VaspUtils.hexStrDecode(testRecord.getPayloadHex());
            val recreatedJson = Json.toJson(vaspMsg);

            JSONAssert.assertEquals(originalJson, recreatedJson, false);

            log.debug("Signature for {} has been checked", testRecord.messageType);
        }
    }

    @Test
    public void makeSignedPayloadTest() {
        SignServiceBaseImpl signServiceBase = new SignServiceImpl(contractService, vaspIdentityService);
        String privateKey = "0x790a3437381e0ca44a71123d56dc64a6209542ddd58e5a56ecdb13134e86f7c6";
        VaspMessage vaspMessage = new SessionRequest();
        VaspMessage.Header header = new VaspMessage.Header();
        header.setMessageId("0x32eaae0fcbf6a342aec65936ea208653");
        header.setSessionId("0xfe3f216d0de7f94ba978225842c7330c");
        header.setMessageType(VaspMessage.TypeDescriptor.SESSION_REQUEST);
        header.setResponseCode("1");
        vaspMessage.setHeader(header);

        String expected = "0x7b226d7367223a7b2274797065223a22313130222c226d73676964223a2230783332656161653066636266366133343261656336353933366561323038363533222c2273657373696f6e223a2230786665336632313664306465376639346261393738323235383432633733333063222c22636f6465223a2231227d2c2268616e647368616b65223a7b7d7d0f1a1e2e53384558b41c85ad802826a14c74d0c8299029c830d0f6706a55cfbc6495ba0f61513ad8f6bb4746064fdcf9c8f18a8967a79638a65734e18adadd081c";
        Assertions.assertEquals(expected, signServiceBase.makeSignedPayload(vaspMessage, privateKey));
    }
}
