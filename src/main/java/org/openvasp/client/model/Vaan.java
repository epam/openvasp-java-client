package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;
import static org.openvasp.client.common.VaspUtils.toBytes;
import static org.openvasp.client.common.VaspUtils.toHex;

/**
 * Virtual Assets Account Number
 *
 * @author Olexandr_Bilovol@epam.com
 */
@EqualsAndHashCode
public final class Vaan implements Serializable {

    private static final String VAAN_FORMAT_ERROR = "VAAN should be a hexadecimal string of the length 24";
    private static final String VAAN_CHECKSUM_ERROR = "Invalid VAAN checksum: expected 0x%s but actual 0x%s";

    @Getter(onMethod_ = {@JsonValue})
    private final String data;

    @JsonCreator
    public Vaan(@NonNull final String data) {
        checkArgument(data.length() == 24, VAAN_FORMAT_ERROR);
        val expectedCheckSum = checkSum8Modulo256(data.substring(0, data.length() - 2));
        val actualCheckSum = data.substring(data.length() - 2);
        checkArgument(
                actualCheckSum.equals(expectedCheckSum),
                String.format(VAAN_CHECKSUM_ERROR, expectedCheckSum, actualCheckSum));
        this.data = data;
    }

    public Vaan(@NonNull final VaspCode vaspCode, @NonNull final String customerNr) {
        checkArgument(customerNr.length() == 14);
        val raw = vaspCode + customerNr;
        this.data = raw + checkSum8Modulo256(raw);
    }

    public VaspCode getVaspCode() {
        return new VaspCode(data.substring(0, 8));
    }

    public String getCustomerNr() {
        return data.substring(8, 22);
    }

    public String getCheckSum() {
        return data.substring(22, 24);
    }

    @Override
    public String toString() {
        return Joiner.on('-').join(Splitter.fixedLength(4).limit(6).splitToList(data));
    }

    private static String checkSum8Modulo256(@NonNull final String vaan) {
        byte[] data = toBytes(vaan);
        byte check = 0;
        for (byte b : data) {
            check += b;
        }
        return toHex(new byte[]{check}, false);
    }

}
