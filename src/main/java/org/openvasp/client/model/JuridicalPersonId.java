package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.NoSuchElementException;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JuridicalPersonId {

    @JsonProperty("jurid")
    private String jurId;

    @JsonProperty("jurid_type")
    private JurIdType jurIdType;

    @JsonProperty("jurid_country")
    private Country jurIdCountry;

    @JsonProperty("jurid_issuer")
    private String jurIdIssuer;

    public enum JurIdType {

        COUNTRY_IDENTIFICATION_NUMBER(1),
        TAX_IDENTIFICATION_NUMBER(2),
        CERTIFICATE_OF_INCORPORATION_NO(3),
        LEGAL_ENTITY_IDENTIFIER(4),
        BANK_PARTY_IDENTIFICATION(5),
        OTHER(6);


        public final int id;

        JurIdType(int id) {
            this.id = id;
        }

        @JsonValue
        public int getId() {
            return id;
        }

        @JsonCreator
        public static JurIdType fromId(int id) {
            for (val item : JurIdType.values()) {
                if (item.id == id) {
                    return item;
                }
            }

            throw new NoSuchElementException(String.format(
                    "%s with id = %d does not exist",
                    JurIdType.class.getName(),
                    id));
        }

    }

}
