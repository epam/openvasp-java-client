package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.openvasp.client.common.VaspValidationException;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@EqualsAndHashCode(of = "vaan")
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Beneficiary {

    @JsonProperty
    private final String name;

    @JsonProperty
    private final Vaan vaan;

    @JsonCreator
    public Beneficiary(
            @JsonProperty("name") final String name,
            @JsonProperty("vaan") final Vaan vaan) {
        this.name = name;
        this.vaan = vaan;
    }

    public void validate(@NonNull final VaspMessage source) {
        if (StringUtils.isEmpty(name)) {
            throw new VaspValidationException(source, "Beneficiary name must be present");
        }
    }

}
