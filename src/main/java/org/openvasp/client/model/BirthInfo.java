package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
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

    public void validate(VaspMessage source) {
        if(null==birthDate)
            throw new VaspValidationException(source, "Birth date must be present");
        
        if(null==birthCity || birthCity.isEmpty())
            throw new VaspValidationException(source, "Birth city must be present");
        
        if(null==birthCountry)
            throw new VaspValidationException(source, "Birth country must be present");
    }
}
