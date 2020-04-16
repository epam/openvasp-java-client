package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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

}
