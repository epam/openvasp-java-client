package org.openvasp.client.crypto;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;

import java.security.SecureRandom;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.openvasp.client.common.VaspUtils.toBytes;
import static org.openvasp.client.common.VaspUtils.toHex;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public final class X25519KeyPair {

    private static final X25519KeyPairGenerator KEY_PAIR_GENERATOR = new X25519KeyPairGenerator();

    private final AsymmetricCipherKeyPair keyPairHolder;

    @Getter
    private final String privateKey;

    @Getter
    private final String publicKey;

    static {
        KEY_PAIR_GENERATOR.init(new KeyGenerationParameters(new SecureRandom(), 0));
    }

    @SneakyThrows
    public static X25519KeyPair generateKey() {
        return new X25519KeyPair(KEY_PAIR_GENERATOR.generateKeyPair());
    }

    public static X25519KeyPair importPrivateKey(@NonNull final String privateKeyHex) {
        val privateKeyParameters = new X25519PrivateKeyParameters(toBytes(privateKeyHex), 0);
        val publicKeyParameters = privateKeyParameters.generatePublicKey();
        return new X25519KeyPair(new AsymmetricCipherKeyPair(publicKeyParameters, privateKeyParameters));
    }

    public String generateSharedSecretHex(@NonNull final String publicKeyHex) {
        val pkForEcsh = (X25519PrivateKeyParameters) keyPairHolder.getPrivate();
        val publicKeyBytes = toBytes(publicKeyHex);
        val result = new byte[32];
        pkForEcsh.generateSecret(new X25519PublicKeyParameters(publicKeyBytes, 0), result, 0);
        return toHex(result, true);
    }

    private X25519KeyPair(@NonNull final AsymmetricCipherKeyPair keyPairHolder) {
        this.keyPairHolder = keyPairHolder;
        this.privateKey = getPrivateKeyHex();
        this.publicKey = getPublicKeyHex();
    }

    private String getPrivateKeyHex() {
        val privateKeyParameters = keyPairHolder.getPrivate();
        checkNotNull(privateKeyParameters);
        checkArgument(privateKeyParameters instanceof X25519PrivateKeyParameters);
        val x25519PrivateKeyParameters = (X25519PrivateKeyParameters) privateKeyParameters;
        return toHex(x25519PrivateKeyParameters.getEncoded(), true);
    }

    private String getPublicKeyHex() {
        val publicKeyParameters = keyPairHolder.getPublic();
        checkNotNull(publicKeyParameters);
        checkArgument(publicKeyParameters instanceof X25519PublicKeyParameters);
        val x25519PublicKeyParameters = (X25519PublicKeyParameters) publicKeyParameters;
        return toHex(x25519PublicKeyParameters.getEncoded(), true);
    }

}
