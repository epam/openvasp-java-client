package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@EqualsAndHashCode(of = {"data"})
public final class VaspCode {

    private static final String VASP_CODE_FORMAT_ERROR = "VASP code should be a hexadecimal string of the length 8";

    @Getter(onMethod_ = {@JsonValue})
    private final String data;

    @JsonCreator
    public VaspCode(@NonNull final String data) {
        checkArgument(data.length() == 8, VASP_CODE_FORMAT_ERROR);
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }

    public Topic toTopic() {
        return new Topic("0x" + data);
    }

}
