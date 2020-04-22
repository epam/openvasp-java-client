package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openvasp.client.common.VaspValidationException;

import java.time.LocalDate;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class BirthInfo {

    @JsonProperty("birthdate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @JsonProperty("birthcity")
    private String birthCity;

    @JsonProperty("birthcountry")
    private Country birthCountry;

    public void validate(@NonNull final VaspMessage source) {
        if (null == birthDate) {
            throw new VaspValidationException(source, "Birth date must be present");
        }

        if (StringUtils.isEmpty(birthCity)) {
            throw new VaspValidationException(source, "Birth city must be present");
        }

        if (null == birthCountry) {
            throw new VaspValidationException(source, "Birth country must be present");
        }
    }

}
