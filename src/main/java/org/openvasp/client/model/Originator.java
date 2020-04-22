package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
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

    public void validate(@NonNull final VaspMessage source) {
        if (StringUtils.isEmpty(name)) {
            throw new VaspValidationException(source, "Originator name must be present");
        }

        if (vaan == null) {
            throw new VaspValidationException(source, "Originator VAAN must be present");
        }

        if (address != null) {
            address.validate(source);
        }

        VaspMessage.checkRules(source, birth, nat, jur, bic);
    }

}
