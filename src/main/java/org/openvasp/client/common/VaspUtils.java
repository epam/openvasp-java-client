package org.openvasp.client.common;

import lombok.NonNull;
import lombok.val;
import org.bouncycastle.util.encoders.Hex;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public final class VaspUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    static {
        SECURE_RANDOM.setSeed(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
    }

    private VaspUtils() {
    }

    public static String newSessionId() {
        return newMessageId();
    }

    public static String newMessageId() {
        byte[] result = new byte[16];
        SECURE_RANDOM.nextBytes(result);
        return toHex(result, true);
    }

    public static String toHex(@NonNull final byte[] bytes, final boolean prefix) {
        val result = new StringBuilder();
        if (prefix) {
            result.append("0x");
        }
        result.append(Hex.toHexString(bytes));
        return result.toString();
    }

    public static String toHex(@NonNull final byte[] bytes) {
        return toHex(bytes, false);
    }

    public static byte[] toBytes(@NonNull final String hex) {
        return hex.startsWith("0x")
                ? Hex.decode(hex.substring(2))
                : Hex.decode(hex);
    }

    public static String hexStrEncode(@NonNull final String str, final boolean prefix) {
        return toHex(str.getBytes(), prefix);
    }

    public static String hexStrEncode(@NonNull final String str) {
        return toHex(str.getBytes(), false);
    }

    public static String hexStrDecode(@NonNull final String hex) {
        return new String(toBytes(hex));
    }

}
