package org.openvasp.client.service.impl;

import org.bouncycastle.util.encoders.Hex;
import org.openvasp.client.service.SignService;
import org.web3j.crypto.*;
import org.web3j.utils.Numeric;

import javax.inject.Singleton;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author Jan_Juraszek@epam.com
 */
@Singleton
public final class SignServiceImpl implements SignService {

    @Override
    public String signPayload(String payload, String privateKey) {
        privateKey = Numeric.cleanHexPrefix(privateKey);
        ECKeyPair keyPair = ECKeyPair.create(Hex.decode(privateKey));
        Sign.SignatureData signature = Sign.signPrefixedMessage(payload.getBytes(StandardCharsets.UTF_8), keyPair);
        String paddedR = Numeric.toHexStringNoPrefixZeroPadded(new BigInteger(1, signature.getR()), 64);
        String paddedS = Numeric.toHexStringNoPrefixZeroPadded(new BigInteger(1, signature.getS()), 64);
        return paddedR + paddedS + Hex.toHexString(signature.getV());
    }

    @Override
    public boolean verifySign(String payload, String sign, String pubKey) {
        pubKey = Numeric.cleanHexPrefix(pubKey);
        pubKey = pubKey.length() > 128 ? pubKey.substring(pubKey.length() - 128) : pubKey; // for compatibility with C# client
        String expectedSignerAddress = Keys.getAddress(pubKey);
        String MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";
        String prefix = MESSAGE_PREFIX + payload.length();
        byte[] msgHash = Hash.sha3((prefix + payload).getBytes());

        byte[] signatureBytes = Numeric.hexStringToByteArray(sign);
        byte v = signatureBytes[64];
        if (v < 27) {
            v += 27;
        }

        Sign.SignatureData sd =
                new Sign.SignatureData(
                        v,
                        (byte[]) Arrays.copyOfRange(signatureBytes, 0, 32),
                        (byte[]) Arrays.copyOfRange(signatureBytes, 32, 64));

        String addressRecovered = null;
        boolean match = false;

        // Iterate for each possible key to recover
        for (int i = 0; i < 4; i++) {
            BigInteger publicKey =
                    Sign.recoverFromSignature(
                            (byte) i,
                            new ECDSASignature(
                                    new BigInteger(1, sd.getR()), new BigInteger(1, sd.getS())),
                            msgHash);

            if (publicKey != null) {
                addressRecovered = Keys.getAddress(publicKey);
                if (addressRecovered.equals(expectedSignerAddress)) {
                    match = true;
                    break;
                }
            }
        }
        return match;
    }

}
