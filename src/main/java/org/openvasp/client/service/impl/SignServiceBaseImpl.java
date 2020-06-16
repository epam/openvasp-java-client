package org.openvasp.client.service.impl;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.openvasp.client.common.Json;
import org.openvasp.client.common.VaspValidationException;
import org.openvasp.client.model.VaspMessage;
import org.openvasp.client.service.ContractService;
import org.openvasp.client.service.SignService;
import org.openvasp.client.service.VaspIdentityService;

import static org.openvasp.client.common.VaspUtils.hexStrDecode;
import static org.openvasp.client.common.VaspUtils.hexStrEncode;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Slf4j
abstract class SignServiceBaseImpl implements SignService {

    private final ContractService contractService;
    private final VaspIdentityService vaspIdentityService;

    SignServiceBaseImpl(
            final ContractService contractService,
            final VaspIdentityService vaspIdentityService) {

        this.contractService = contractService;
        this.vaspIdentityService = vaspIdentityService;
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
        log.debug("RECEIVE: {}", json);

        val signature = StringUtils.right(whisperPayload, signatureLength());

        val message = VaspMessage.fromJson(json);
        message.validate();

        val senderContractAddress = vaspIdentityService.resolveSenderVaspId(message)
                .orElseThrow(() -> new VaspValidationException(message, "Sender's VASP ID cannot be resolved"));

        val senderContract = contractService.getVaspContractInfo(senderContractAddress);
        val publicSigningKey = senderContract.getSigningKey();

        if (!verifySign(payload, signature, publicSigningKey)) {
            throw new VaspValidationException(
                    message,
                    "Invalid signature for incoming message");
        }

        return message;
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
