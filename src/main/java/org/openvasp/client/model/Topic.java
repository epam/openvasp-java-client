package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.bouncycastle.util.BigIntegers;

import java.security.SecureRandom;

import static com.google.common.base.Preconditions.checkArgument;
import static org.openvasp.client.common.VaspUtils.isValidHex;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@EqualsAndHashCode(of = {"data"})
public final class Topic {

    private static final String TOPIC_FORMAT_ERROR = "Whisper topic code should be a hexadecimal string of the length 10 prefixed with 0x";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Getter(onMethod_ = {@JsonValue})
    private final String data;

    @JsonCreator
    public Topic(@NonNull final String data) {
        checkArgument(data.length() == 10 && isValidHex(data), TOPIC_FORMAT_ERROR);
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }

    public static Topic newRandom() {
        val randomValue = BigIntegers.createRandomBigInteger(32, SECURE_RANDOM);
        return new Topic(String.format("0x%08x", randomValue));
    }

}
