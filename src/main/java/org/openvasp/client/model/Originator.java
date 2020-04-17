package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.openvasp.client.common.VaspValidationException;

import java.util.List;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Originator {

    @JsonProperty("name")
    private String name;

    @JsonProperty("vaan")
    private Vaan vaan;

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

    public void validate(VaspMessage source) {
        if(null==name || name.isEmpty())
            throw new VaspValidationException(source, "Originator name must be present");
        
        if(null==vaan)
            throw new VaspValidationException(source, "Originator VAAN must be present");
        
        if(null!=address) 
            address.validate(source);
        
        VaspMessage.checkRules(source, birth, nat, jur, bic);
    }
}
