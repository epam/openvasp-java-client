package org.openvasp.client.service.impl;

import lombok.NonNull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.openvasp.client.common.Json;
import org.openvasp.client.common.VaspValidationException;
import org.openvasp.client.model.VaspMessage;
import org.openvasp.client.service.ContractService;
import org.openvasp.client.service.SignService;

import static org.openvasp.client.common.VaspUtils.hexStrDecode;
import static org.openvasp.client.common.VaspUtils.hexStrEncode;

/**
 * @author Olexandr_Bilovol@epam.com
 */
abstract class SignServiceBaseImpl implements SignService {

    private final ContractService contractService;

    SignServiceBaseImpl(final ContractService contractService) {
        this.contractService = contractService;
    }

    @Override
    public String makeSignedPayload(@NonNull final VaspMessage message, @NonNull final String privateKey) {
        val encodedJson = hexStrEncode(Json.toJson(message), true);
        val signature = signPayload(encodedJson, privateKey);
        return encodedJson + signature;
    }

    @Override
    public VaspMessage extractSignedMessage(@NonNull final String whisperPayload) {
        val payload = StringUtils.left(whisperPayload, whisperPayload.length() - signatureLength());
        val json = hexStrDecode(payload);
        val signature = StringUtils.right(whisperPayload, signatureLength());

        val vaspMessage = VaspMessage.fromJson(json);
        vaspMessage.validate();

        val senderVaspCode = vaspMessage.getVaspInfo().getVaspCode();
        val senderContract = contractService.getVaspContractInfo(senderVaspCode);
        val publicSigningKey = senderContract.getSigningKey();

        if (!verifySign(payload, signature, publicSigningKey)) {
            throw new VaspValidationException(
                    vaspMessage,
                    "Invalid signature for incoming message");
        }

        return vaspMessage;
    }

    abstract int signatureLength();

    /**
     * Sign the message with given private key
     *
     * @param payload    a hex-encoded message
     * @param privateKey a hex-encoded private key
     * @return hex-encoded signature
     */
    abstract String signPayload(String payload, String privateKey);

    /**
     * Verify whether the provided signature for the given message is valid
     *
     * @param payload a hex-encoded message
     * @param sign    a hex-encoded signature
     * @param pubKey  a hex-encoded public key
     * @return boolean true if the signature is valid, false otherwise
     */
    abstract boolean verifySign(String payload, String sign, String pubKey);

}
