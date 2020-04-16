package org.openvasp.client.model;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openvasp.client.common.Json.*;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public class JsonSerializationTests {

    @Test
    public void country() {
        val af1 = Country.ALL.get("AF");
        val jsonStr = toJson(af1);
        assertThat(jsonStr).isEqualTo("\"AF\"");
        val af2 = fromJson(Country.class, jsonStr);
        assertThat(af2).isSameAs(af1);
    }

    @Test
    @SneakyThrows
    public void beneficiary1() {
        val beneficiary = loadTestJson(Beneficiary.class, "beneficiary-1.json");
        assertThat(beneficiary.getName()).isEqualTo("John Smith");

        val vaan = beneficiary.getVaan();
        assertThat(vaan.getData()).isEqualTo("a0b1c2d3a0b1c2d3e4f5ffa4");
        assertThat(vaan.getCustomerNr()).isEqualTo("a0b1c2d3e4f5ff");
        assertThat(vaan.getCheckSum()).isEqualTo("a4");

        val vaspCode = vaan.getVaspCode();
        assertThat(vaspCode.toString()).isEqualTo("a0b1c2d3");

        JSONAssert.assertEquals(
                loadTestJson("beneficiary-1.json"),
                toJson(beneficiary),
                false);
    }

    @Test
    public void beneficiary2() {
        assertThrows(
                JsonMappingException.class,
                () -> loadTestJson(Beneficiary.class, "beneficiary-2.json"));
    }

    @Test
    @SneakyThrows
    public void birthInfo() {
        val birthinfo = loadTestJson(BirthInfo.class, "birth-info.json");
        assertThat(birthinfo.getBirthDate()).isEqualTo("1995-01-02");
        assertThat(birthinfo.getBirthCity()).isEqualTo("Zurich");
        assertThat(birthinfo.getBirthCountry()).isEqualTo(Country.ALL.get("CH"));
        
        JSONAssert.assertEquals(
                loadTestJson("birth-info.json"),
                toJson(birthinfo),
                false);
    }
    
    @Test
    @SneakyThrows
    public void juridicalPersonId() {
        val juridicalpersonid = loadTestJson(JuridicalPersonId.class, "juridical-person-id.json");
        assertThat(juridicalpersonid.getJurId()).isEqualTo("12345ABC");
        assertThat(juridicalpersonid.getJurIdCountry()).isEqualTo(Country.ALL.get("CH"));
        assertThat(juridicalpersonid.getJurIdIssuer()).isEqualTo("Business and Enterprise Register");
        assertThat(juridicalpersonid.getJurIdType()).isEqualTo(JuridicalPersonId.JurIdType.COUNTRY_IDENTIFICATION_NUMBER);
        
        JSONAssert.assertEquals(
                loadTestJson("juridical-person-id.json"),
                toJson(juridicalpersonid),
                false);
    }
    
    @Test
    @SneakyThrows
    public void naturalPersonId() {
        val naturalpersonid = loadTestJson(NaturalPersonId.class, "natural-person-id.json");
        assertThat(naturalpersonid.getNatId()).isEqualTo("756.1234.5678.90");
        assertThat(naturalpersonid.getNatIdCountry()).isEqualTo(Country.ALL.get("CH"));
        assertThat(naturalpersonid.getNatIdIssuer()).isEqualTo("Federal Department of Justice and Police");
        assertThat(naturalpersonid.getNatIdType()).isEqualTo(NaturalPersonId.NatIdType.NATIONAL_IDENTITY_NUMBER);
        
        JSONAssert.assertEquals(
                loadTestJson("natural-person-id.json"),
                toJson(naturalpersonid),
                false);
    }
    
    @Test
    @SneakyThrows
    public void originator1() {
        val origin = loadTestJson(Originator.class, "originator.json");
        assertThat(origin.getName()).isEqualTo("John Smith");
        
        val vaan = origin.getVaan();
        assertThat(vaan.getData()).isEqualTo("a0b1c2d3a0b1c2d3e4f5ffa4");
        assertThat(vaan.getCustomerNr()).isEqualTo("a0b1c2d3e4f5ff");
        assertThat(vaan.getCheckSum()).isEqualTo("a4");
        
        val address = origin.getAddress();
        assertThat(address.getStreet()).isEqualTo("Kappelergasse");
        assertThat(address.getNumber()).isEqualTo("1");
        assertThat(address.getAdrline()).isEqualTo("Fraumunsterpost");
        assertThat(address.getPostCode()).isEqualTo("8022");
        assertThat(address.getTown()).isEqualTo("Zurich");
        assertThat(address.getCountry()).isEqualTo(Country.ALL.get("CH"));
        
        val birth = origin.getBirth();
        assertThat(birth.getBirthDate()).isEqualTo("1995-01-02");
        assertThat(birth.getBirthCity()).isEqualTo("Zurich");
        assertThat(birth.getBirthCountry()).isEqualTo(Country.ALL.get("CH"));
        
        val nat = origin.getNat();
        assertThat(nat.size()).isEqualTo(1);
        assertThat(nat.get(0).getNatId()).isEqualTo("756.1234.5678.90");
        assertThat(nat.get(0).getNatIdCountry()).isEqualTo(Country.ALL.get("CH"));
        assertThat(nat.get(0).getNatIdIssuer()).isEqualTo("Federal Department of Justice and Police");
        assertThat(nat.get(0).getNatIdType()).isEqualTo(NaturalPersonId.NatIdType.NATIONAL_IDENTITY_NUMBER);
        
        JSONAssert.assertEquals(
                loadTestJson("originator.json"),
                toJson(origin),
                false);
    }

    @Test
    public void originator2() {
        // TODO: check if throws when rules in section 7.11.1 not met
    }
    
    @Test
    @SneakyThrows
    public void postalAddress() {
        val postaladdress = loadTestJson(PostalAddress.class, "postal-address.json");
        assertThat(postaladdress.getStreet()).isEqualTo("Kappelergasse");
        assertThat(postaladdress.getNumber()).isEqualTo("1");
        assertThat(postaladdress.getAdrline()).isEqualTo("Fraumunsterpost");
        assertThat(postaladdress.getPostCode()).isEqualTo("8022");
        assertThat(postaladdress.getTown()).isEqualTo("Zurich");
        assertThat(postaladdress.getCountry()).isEqualTo(Country.ALL.get("CH"));
        
        JSONAssert.assertEquals(
                loadTestJson("postal-address.json"),
                toJson(postaladdress),
                false);
    }
}
