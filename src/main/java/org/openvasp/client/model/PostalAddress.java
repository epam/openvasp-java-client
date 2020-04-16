package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

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

}
