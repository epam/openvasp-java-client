package org.openvasp.client.crypto;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;

import java.math.BigInteger;
import java.security.SecureRandom;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.bouncycastle.util.BigIntegers.asUnsignedByteArray;
import static org.openvasp.client.common.VaspUtils.toBytes;
import static org.openvasp.client.common.VaspUtils.toHex;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public final class ECDHKeyPair {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final ECDomainParameters DOMAIN;
    private static final ECKeyPairGenerator KEY_PAIR_GENERATOR = new ECKeyPairGenerator();

    private final AsymmetricCipherKeyPair keyPairHolder;

    @Getter
    private final String privateKey;

    @Getter
    private final String publicKey;

    static {
        val secp256k1 = CustomNamedCurves.getByName("secp256k1");

        DOMAIN = new ECDomainParameters(
                secp256k1.getCurve(),
                secp256k1.getG(),
                secp256k1.getN(),
                secp256k1.getH());

        KEY_PAIR_GENERATOR.init(new ECKeyGenerationParameters(DOMAIN, SECURE_RANDOM));
    }

    public static ECDHKeyPair generateKeyPair() {
        return new ECDHKeyPair(KEY_PAIR_GENERATOR.generateKeyPair());
    }

    public static ECDHKeyPair importPrivateKey(@NonNull final String privateKeyHex) {
        val d = new BigInteger(1, toBytes(privateKeyHex));
        val ecPrivateKeyParameters = new ECPrivateKeyParameters(d, DOMAIN);
        val q = (new FixedPointCombMultiplier()).multiply(DOMAIN.getG(), d);
        val ecPublicKeyParameters = new ECPublicKeyParameters(q, DOMAIN);
        return new ECDHKeyPair(new AsymmetricCipherKeyPair(ecPublicKeyParameters, ecPrivateKeyParameters));
    }

    public String generateSharedSecretHex(@NonNull final String publicKeyHex) {
        val publicKeyBytes = toBytes(publicKeyHex);
        val q = DOMAIN.getCurve().decodePoint(publicKeyBytes);
        val ecPublicKeyParameters = new ECPublicKeyParameters(q, DOMAIN);
        val ecdhBasicAgreement = new ECDHBasicAgreement();
        ecdhBasicAgreement.init(keyPairHolder.getPrivate());
        val ecdhBasicAgreementResult = ecdhBasicAgreement.calculateAgreement(ecPublicKeyParameters);
        return toHex(asUnsignedByteArray(ecdhBasicAgreement.getFieldSize(), ecdhBasicAgreementResult), true);
    }

    private ECDHKeyPair(@NonNull final AsymmetricCipherKeyPair keyPairHolder) {
        this.keyPairHolder = keyPairHolder;
        this.privateKey = getPrivateKeyHex();
        this.publicKey = getPublicKeyHex();
    }

    private String getPrivateKeyHex() {
        val privateKeyParameters = keyPairHolder.getPrivate();
        checkNotNull(privateKeyParameters);
        checkArgument(privateKeyParameters instanceof ECPrivateKeyParameters);
        val ecPrivateKeyParameters = (ECPrivateKeyParameters) privateKeyParameters;
        return toHex(asUnsignedByteArray(ecPrivateKeyParameters.getD()), true);
    }

    private String getPublicKeyHex() {
        val publicKeyParameters = keyPairHolder.getPublic();
        checkNotNull(publicKeyParameters);
        checkArgument(publicKeyParameters instanceof ECPublicKeyParameters);
        val ecPublicKeyParameters = (ECPublicKeyParameters) publicKeyParameters;
        return toHex(ecPublicKeyParameters.getQ().getEncoded(false), true);
    }

}
