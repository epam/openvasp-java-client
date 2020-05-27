package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import org.openvasp.client.common.VaspValidationException;

import java.util.NoSuchElementException;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class NaturalPersonId {

    @JsonProperty("natid")
    private String natId;

    @JsonProperty("natid_type")
    private NatIdType natIdType;

    @JsonProperty("natid_country")
    private Country natIdCountry;

    @JsonProperty("natid_issuer")
    private String natIdIssuer;

    public void validate(@NonNull final VaspMessage source) {
        // whitepaper sections 7.10.1 and 7.11.1, rule 5)
        if (null == natIdCountry && (null == natIdIssuer || natIdIssuer.isEmpty())) {
            throw new VaspValidationException(source, "Either [natid_country] or [natid_issuer] or both must be present");
        }
    }

    public enum NatIdType {

        PASSPORT_NUMBER(1),
        NATIONAL_IDENTITY_NUMBER(2),
        SOCIAL_SECURITY_NUMBER(3),
        TAX_IDENTIFICATION_NUMBER(4),
        ALIEN_REGISTRATION_NUMBER(5),
        DRIVERS_LICENSE_NUMBER(6),
        OTHER(7);

        public final int id;

        NatIdType(int id) {
            this.id = id;
        }

        @JsonValue
        public int getId() {
            return id;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static NatIdType fromId(int id) {
            for (val item : NatIdType.values()) {
                if (item.id == id) {
                    return item;
                }
            }

            throw new NoSuchElementException(String.format(
                    "%s with id = %d does not exist",
                    NatIdType.class.getName(),
                    id));
        }

    }

}
