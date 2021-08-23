package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.snksoft.crc.CRC;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;
import static org.openvasp.client.common.VaspUtils.toBytes;

/**
 * Virtual Assets Account Number
 *
 * @author Olexandr_Bilovol@epam.com
 */
@EqualsAndHashCode
public final class Vaan implements Serializable {

    private static final String VAAN_FORMAT_ERROR = "VAAN should be a hexadecimal string of the length 24";
    private static final String VAAN_CHECKSUM_ERROR = "Invalid VAAN checksum: expected 0x%s but actual 0x%s";
    private static final CRC.Parameters CRC8_WCDMA = new CRC.Parameters(8, 0x9B, 0x00, true, true, 0x00);

    @Getter(onMethod_ = {@JsonValue})
    private final String data;

    @JsonCreator
    public Vaan(@NonNull final String data) {
        String dataWithoutWhitespace = StringUtils.deleteWhitespace(data).toLowerCase();
        checkArgument(dataWithoutWhitespace.length() == 24, VAAN_FORMAT_ERROR);
        val expectedCheckSum = checkSumCrc8Wcdma(dataWithoutWhitespace.substring(0,
                dataWithoutWhitespace.length() - 2));
        val actualCheckSum = dataWithoutWhitespace.substring(dataWithoutWhitespace.length() - 2);
        checkArgument(
                actualCheckSum.equals(expectedCheckSum),
                String.format(VAAN_CHECKSUM_ERROR, expectedCheckSum, actualCheckSum));
        this.data = dataWithoutWhitespace;
    }

    public Vaan(@NonNull final VaspCode vaspCode, @NonNull final String customerNr) {
        checkArgument(customerNr.length() == 10);
        val raw = vaspCode + customerNr;
        this.data = raw + checkSumCrc8Wcdma(raw);
    }

    public VaspCodeType getVaspCodeType() { return new VaspCodeType(data.substring(0, 2)); }

    public VaspCode getVaspCode() { return new VaspCode(data.substring(4, 12)); }

    public String getCustomerNr() {
        return data.substring(12, 22);
    }

    public String getCheckSum() {
        return data.substring(22, 24);
    }

    @Override
    public String toString() {
        return Joiner.on(' ').join(Splitter.fixedLength(4).limit(6).splitToList(data)).toLowerCase();
    }

    private static String checkSumCrc8Wcdma(@NonNull final String vaan) {
        byte[] data = toBytes(vaan);
        CRC crc = new CRC(CRC8_WCDMA);
        long result = crc.calculateCRC(data);
        return Long.toHexString(result);
    }

}
