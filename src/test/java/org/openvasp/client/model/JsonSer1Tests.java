package org.openvasp.client.model;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.openvasp.client.common.JsonPathFixture;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openvasp.client.common.Json.*;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public class JsonSer1Tests {

    private static final String SER_DATA_FOLDER = "serialization";

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
        val jsonFileName = SER_DATA_FOLDER + "/beneficiary-1.json";
        val beneficiary = loadTestJson(Beneficiary.class, jsonFileName);
        val originatorFixture = new JsonPathFixture(loadTestJson(jsonFileName));
        assertThat(beneficiary.getName()).isEqualTo("John Smith");

        val vaan = beneficiary.getVaan();
        originatorFixture.assertEquals(vaan.getData(), "$.vaan");
        assertThat(vaan.getCustomerNr()).isEqualTo("c2d3e4f5ff");
        assertThat(vaan.getCheckSum()).isEqualTo("63");

        val vaspCode = vaan.getVaspCode();
        assertThat(vaspCode.toString()).isEqualTo("a0b1c2d3");

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(beneficiary), false);
    }

    @Test
    public void beneficiary2() {
        val jsonFileName = SER_DATA_FOLDER + "/beneficiary-2.json";
        assertThrows(
                JsonMappingException.class,
                () -> loadTestJson(Beneficiary.class, jsonFileName));
    }

    @Test
    @SneakyThrows
    public void birthInfo() {
        val jsonFileName = SER_DATA_FOLDER + "/birth-info.json";
        val birthinfo = loadTestJson(BirthInfo.class, jsonFileName);

        assertThat(birthinfo.getBirthDate()).isEqualTo("1995-01-02");
        assertThat(birthinfo.getBirthCity()).isEqualTo("Zurich");
        assertThat(birthinfo.getBirthCountry()).isEqualTo(Country.ALL.get("CH"));

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(birthinfo), false);
    }

    @Test
    @SneakyThrows
    public void juridicalPersonId() {
        val jsonFileName = SER_DATA_FOLDER + "/juridical-person-id.json";
        val juridicalPersonId = loadTestJson(JuridicalPersonId.class, jsonFileName);

        assertThat(juridicalPersonId.getJurId()).isEqualTo("12345ABC");
        assertThat(juridicalPersonId.getJurIdCountry()).isEqualTo(Country.ALL.get("CH"));
        assertThat(juridicalPersonId.getJurIdIssuer()).isEqualTo("Business and Enterprise Register");
        assertThat(juridicalPersonId.getJurIdType()).isEqualTo(JuridicalPersonId.JurIdType.COUNTRY_IDENTIFICATION_NUMBER);

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(juridicalPersonId), false);
    }

    @Test
    @SneakyThrows
    public void naturalPersonId() {
        val jsonFileName = SER_DATA_FOLDER + "/natural-person-id.json";
        val naturalpersonid = loadTestJson(NaturalPersonId.class, jsonFileName);

        assertThat(naturalpersonid.getNatId()).isEqualTo("756.1234.5678.90");
        assertThat(naturalpersonid.getNatIdCountry()).isEqualTo(Country.ALL.get("CH"));
        assertThat(naturalpersonid.getNatIdIssuer()).isEqualTo("Federal Department of Justice and Police");
        assertThat(naturalpersonid.getNatIdType()).isEqualTo(NaturalPersonId.NatIdType.NATIONAL_IDENTITY_NUMBER);

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(naturalpersonid), false);
    }

    @Test
    @SneakyThrows
    public void originator() {
        val jsonFileName = SER_DATA_FOLDER + "/originator.json";
        val originator = loadTestJson(Originator.class, jsonFileName);
        val originatorFixture = new JsonPathFixture(loadTestJson(jsonFileName));

        assertThat(originator.getName()).isEqualTo("John Smith");

        val vaan = originator.getVaan();
        originatorFixture.assertEquals(vaan.getData(), "$.vaan");
        assertThat(vaan.getCustomerNr()).isEqualTo("c2d3e4f5ff");
        assertThat(vaan.getVaspCodeType()).isEqualTo("10");
        assertThat(vaan.getCheckSum()).isEqualTo("63");

        val address = originator.getAddress();
        assertThat(address.getStreet()).isEqualTo("Kappelergasse");
        assertThat(address.getNumber()).isEqualTo("1");
        assertThat(address.getAdrline()).isEqualTo("Fraumunsterpost");
        assertThat(address.getPostCode()).isEqualTo("8022");
        assertThat(address.getTown()).isEqualTo("Zurich");
        assertThat(address.getCountry()).isEqualTo(Country.ALL.get("CH"));

        val birth = originator.getBirth();
        assertThat(birth.getBirthDate()).isEqualTo("1995-01-02");
        assertThat(birth.getBirthCity()).isEqualTo("Zurich");
        assertThat(birth.getBirthCountry()).isEqualTo(Country.ALL.get("CH"));

        val nat = originator.getNat();
        assertThat(nat.size()).isEqualTo(1);
        assertThat(nat.get(0).getNatId()).isEqualTo("756.1234.5678.90");
        assertThat(nat.get(0).getNatIdCountry()).isEqualTo(Country.ALL.get("CH"));
        assertThat(nat.get(0).getNatIdIssuer()).isEqualTo("Federal Department of Justice and Police");
        assertThat(nat.get(0).getNatIdType()).isEqualTo(NaturalPersonId.NatIdType.NATIONAL_IDENTITY_NUMBER);

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(originator), false);
    }

    @Test
    @SneakyThrows
    public void postalAddress() {
        val jsonFileName = SER_DATA_FOLDER + "/postal-address.json";
        val postalAddress = loadTestJson(PostalAddress.class, jsonFileName);

        assertThat(postalAddress.getStreet()).isEqualTo("Kappelergasse");
        assertThat(postalAddress.getNumber()).isEqualTo("1");
        assertThat(postalAddress.getAdrline()).isEqualTo("Fraumunsterpost");
        assertThat(postalAddress.getPostCode()).isEqualTo("8022");
        assertThat(postalAddress.getTown()).isEqualTo("Zurich");
        assertThat(postalAddress.getCountry()).isEqualTo(Country.ALL.get("CH"));

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(postalAddress), false);
    }

}
