package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author krisztian_csepi@epam.com
 */
@EqualsAndHashCode(of = {"data"})
public final class VaspCodeType implements Serializable {

    private static final String VASP_CODE_TYPE_FORMAT_ERROR = "VASP code type should be a hexadecimal string of the length 2";
    private static final String INVALID_VASP_CODE_TYPE_ERROR = "Invalid VASP code type. It has to be: 10 or [a-f]0";

    @Getter(onMethod_ = {@JsonValue})
    private final String data;

    @JsonCreator
    public VaspCodeType(@NonNull final String data) {
        checkArgument(data.length() == 2, VASP_CODE_TYPE_FORMAT_ERROR);
        Pattern pattern = Pattern.compile("10|[a-f]0", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(data);
        checkArgument(matcher.matches(), INVALID_VASP_CODE_TYPE_ERROR);
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }

}
