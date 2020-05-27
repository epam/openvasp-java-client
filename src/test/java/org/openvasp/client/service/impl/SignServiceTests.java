package org.openvasp.client.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.assertThat;

public class SignServiceTests {

    @Data
    public static class SignTestItem {
        @JsonProperty("payload")
        String payloadWithSignature;

        @JsonProperty("key")
        String privateSigningKey;
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
        val testItems = Json.loadTestYaml(SignTestList.class, "signature/signed-messages.yaml");

        for (val v : testItems.content) {
            // derive public key from private key and create a mock SignService instance
            val keyPair = ECKeyPair.create(Hex.decode(Numeric.cleanHexPrefix(v.privateSigningKey)));
            val derivedPublicKey = Numeric.toHexStringWithPrefix(keyPair.getPublicKey());
            val contractInfo = new VaspContractInfo();
            contractInfo.setSigningKey(derivedPublicKey);

            val signService = new SignServiceImpl(new ContractService() {
                @Override
                public VaspContractInfo getVaspContractInfo(EthAddr vaspSmartContractAddress) {
                    return getVaspContractInfo(vaspSmartContractAddress.toVaspCode());
                }

                @Override
                public VaspContractInfo getVaspContractInfo(VaspCode vaspCode) {
                    return contractInfo;
                }
            });

            // create signature from original payload and verify if it's same as the original signature
            val payloadWithoutSignature = StringUtils.left(
                    v.payloadWithSignature, v.payloadWithSignature.length() - SignServiceImpl.SIGNATURE_LENGTH);
            val originalSignature = v.payloadWithSignature.substring(payloadWithoutSignature.length());
            val recalculatedSignature = signService.signPayload(payloadWithoutSignature, v.privateSigningKey);
            assertThat(recalculatedSignature).isEqualTo(originalSignature);

            // verify original signature against original payload and, if correct, validate and extract the message
            val vaspMsg = signService.extractSignedMessage(v.payloadWithSignature);

            val originalJson = VaspUtils.hexStrDecode(payloadWithoutSignature);
            val recreatedJson = Json.toJson(vaspMsg);

            JSONAssert.assertEquals(originalJson, recreatedJson, false);
        }
    }

}
