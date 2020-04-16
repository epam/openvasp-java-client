package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public final class VaspInfo {

    @JsonProperty("name")
    private String name;

    @JsonProperty("id")
    private VaspCode vaspCode;

    @JsonProperty("pk")
    private String pk;

    @JsonProperty("address")
    private PostalAddress address;

    @JsonProperty("birth")
    private BirthInfo birth;

    @JsonProperty("nat")
    private List<NaturalPersonId> nat;

    @JsonProperty("jur")
    private List<JuridicalPersonId> jur;

    @JsonProperty("bic")
    private String bic;

}
