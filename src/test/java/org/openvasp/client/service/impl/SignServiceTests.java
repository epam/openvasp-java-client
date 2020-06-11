package org.openvasp.client.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;
import org.openvasp.client.common.Constants;
import org.openvasp.client.common.Json;
import org.openvasp.client.common.VaspUtils;
import org.openvasp.client.model.EthAddr;
import org.openvasp.client.model.VaspCode;
import org.openvasp.client.model.VaspContractInfo;
import org.openvasp.client.service.ContractService;
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
public class SignServiceTests {

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

}
