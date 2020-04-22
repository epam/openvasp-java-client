package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import static com.google.common.base.Preconditions.checkArgument;
import static org.openvasp.client.common.VaspUtils.isValidHex;

/**
 * Ethereum address
 *
 * @author Olexandr_Bilovol@epam.com
 */
@EqualsAndHashCode(of = {"data"})
public final class EthAddr {

    private static final String ETH_ADDR_FORMAT_ERROR = "Ethereum address should be a hexadecimal string of the length 40 prefixed with 0x";

    @Getter(onMethod_ = {@JsonValue})
    private final String data;

    @JsonCreator
    public EthAddr(@NonNull final String data) {
        checkArgument(data.length() == 42 && isValidHex(data), ETH_ADDR_FORMAT_ERROR);
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }

    public VaspCode toVaspCode() {
        return new VaspCode(StringUtils.right(data, 8));
    }

}
