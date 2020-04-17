package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.openvasp.client.common.VaspValidationException;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PostalAddress {

    @JsonProperty("street")
    private String street;

    @JsonProperty("number")
    private String number;

    @JsonProperty("adrline")
    private String adrline;

    @JsonProperty("postcode")
    private String postCode;

    @JsonProperty("town")
    private String town;

    @JsonProperty("country")
    private Country country;

    public void validate(VaspMessage source) {
        if(null==postCode || postCode.isEmpty())
            throw new VaspValidationException(source, "Post code must be present");

        if(null==town || town.isEmpty())
            throw new VaspValidationException(source, "Town name must be present");

        if(null==country)
            throw new VaspValidationException(source, "Country code must be present");
        
        // whitepaper sections 7.10.1 and 7.11.1, rule 1)
        if ( (null == adrline || adrline.isEmpty()) && 
             (null == street || street.isEmpty() || null == number || number.isEmpty()) ) {
            throw new VaspValidationException(source,
                    "Postal address rules not met - either [street] and [number] must both be present or [adrline]");
        }

    }
}
