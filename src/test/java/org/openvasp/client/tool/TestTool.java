package org.openvasp.client.tool;


import lombok.SneakyThrows;
import lombok.val;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.encoders.Hex;
import org.openvasp.client.crypto.ECDHKeyPair;
import org.openvasp.client.crypto.X25519KeyPair;
import org.openvasp.client.model.Vaan;
import org.openvasp.client.model.VaspCode;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public final class TestTool {

    private TestTool() {
    }

    @SuppressWarnings("unused")
    private static void createEcdhKeyPair() {
        val keyPair1 = ECDHKeyPair.generateKeyPair();
        System.out.format("Private key\t\t %s%nPublic key\t\t %s%n",
                keyPair1.getPrivateKey(),
                keyPair1.getPublicKey());

        System.out.println();

        val keyPair2 = ECDHKeyPair.importPrivateKey(keyPair1.getPrivateKey());
        System.out.format("Private key\t\t %s%nPublic key\t\t %s%n",
                keyPair2.getPrivateKey(),
                keyPair2.getPublicKey());

        System.out.println();

        if (keyPair1.getPrivateKey().equals(keyPair2.getPrivateKey()) &&
                keyPair1.getPublicKey().equals(keyPair2.getPublicKey())) {
            System.out.println("key1 == key2");
        } else {
            System.out.println("key1 != key2");
        }

        System.out.println();

        val keyPair3 = ECDHKeyPair.generateKeyPair();
        System.out.format("SharedSecretHex\t %s%n", keyPair3.generateSharedSecretHex(keyPair1.getPublicKey()));
    }

    @SuppressWarnings("unused")
    private static void createX25519KeyPair() {
        val keyPair1 = X25519KeyPair.generateKey();
        System.out.format("Private key\t\t %s%nPublic key\t\t %s%n",
                keyPair1.getPrivateKey(),
                keyPair1.getPublicKey());

        System.out.println();

        val keyPair2 = X25519KeyPair.importPrivateKey(keyPair1.getPrivateKey());
        System.out.format("Private key\t\t %s%nPublic key\t\t %s%n",
                keyPair2.getPrivateKey(),
                keyPair2.getPublicKey());

        System.out.println();

        if (keyPair1.getPrivateKey().equals(keyPair2.getPrivateKey()) &&
                keyPair1.getPublicKey().equals(keyPair2.getPublicKey())) {
            System.out.println("key1 == key2");
        } else {
            System.out.println("key1 != key2");
        }

        System.out.println();

        val keyPair3 = X25519KeyPair.generateKey();
        System.out.format("SharedSecretHex\t %s%n", keyPair3.generateSharedSecretHex(keyPair1.getPublicKey()));
    }

    @SneakyThrows
    @SuppressWarnings("unused")
    private static void createEcKeyPair() {
        val ecKeyPair = Keys.createEcKeyPair();
        val privateKey = Hex.toHexString(BigIntegers.asUnsignedByteArray(ecKeyPair.getPrivateKey()));
        val publicKey = Hex.toHexString(BigIntegers.asUnsignedByteArray(ecKeyPair.getPublicKey()));
        val credentials = Credentials.create(ecKeyPair);
        System.out.format("PrivateKey=0x%s%n", privateKey);
        System.out.format("PublicKey=0x%s%n", publicKey);
        System.out.format("Address=%s%n", credentials.getAddress());
    }

    @SuppressWarnings("unused")
    private static void generateVaans(final String vaspCodeHex, int start, int count) {
        val vaspCode = new VaspCode(vaspCodeHex);
        for (int i = start; i < start + count; i++) {
            val customerNr = String.format("%010x%04x", 0, i);
            val vaan = new Vaan(vaspCode, customerNr);
            System.out.println(vaan.getData());
        }
    }

    @SuppressWarnings("unused")
    private static void generateVaans() {
        generateVaans("7dface61", 1, 3);
        generateVaans("bbb4ee5c", 5, 3);
        generateVaans("1cd48daa", 10, 3);
    }

    public static void main(String[] args) throws Exception {
        generateVaans();
    }

}
