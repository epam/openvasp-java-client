package org.openvasp.client.model;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

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
    public void originator() {
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

    @Test
    @SneakyThrows
    public void sessionRequest() {
        val sessionRequest = loadTestJson(SessionRequest.class, "session-request.json");
        sessionRequest.validate();
        
        val msg = sessionRequest.getHeader();
        assertThat(msg).isNotNull();
        assertThat(msg.getMessageType()).isEqualTo(VaspMessage.TypeDescriptor.SESSION_REQUEST);
        assertThat(msg.getMessageId()).isEqualTo("0xda770238290a75408b8397d0905d21bf");
        assertThat(msg.getSessionId()).isEqualTo("0x8e8667b04d7ef44b8ae5617b472a0108");
        assertThat(msg.getResponseCode()).isEqualTo("1");
        
        val handshake = sessionRequest.getHandshake();
        assertThat(handshake).isNotNull();
        assertThat(handshake.getTopicA().getData()).isEqualTo("0xa4356c40");
        assertThat(handshake.getSessionPublicKey()).isEqualTo("0xcaf60628b7b009439928890398cb11c7ff5c4d880f7c5304834576f3e5de0679a6a64b5e6b423a369a3c6a7e756c133118994d94906a490840881e7b0acc1674");
        
        val vasp = sessionRequest.getVaspInfo();
        assertThat(vasp).isNotNull();
        assertThat(vasp.getName()).isEqualTo("TestVaspContractPerson");
        assertThat(vasp.getVaspId().toString()).isEqualTo("0x6befaf0656b953b188a0ee3bf3db03d07dface61");
        assertThat(vasp.getPk()).isEqualTo("0x043061dce78a75a970fe7ff297870023931983982cb8615bfd9fe72c52f0040b6bf5e1a6c15085170eac9c584b2bf72d6296949e7ed60caaccf0c4f6ec0d330a5d");
        
        val address = vasp.getAddress();
        assertThat(address).isNotNull();
        assertThat(address.getStreet()).isEqualTo("Some StreetName ");
        assertThat(address.getNumber()).isEqualTo("64");
        assertThat(address.getAdrline()).isEqualTo("Some AddressLine");
        assertThat(address.getPostCode()).isEqualTo("310031");
        assertThat(address.getTown()).isEqualTo("TownN");
        assertThat(address.getCountry()).isEqualTo(Country.ALL.get("DE"));
        
        val birth = vasp.getBirth();
        assertThat(birth).isNotNull();
        assertThat(birth.getBirthDate()).isEqualTo("2020-04-28");
        assertThat(birth.getBirthCity()).isEqualTo("Town X");
        assertThat(birth.getBirthCountry()).isEqualTo(Country.ALL.get("DE"));
        
        val nat = vasp.getNat();
        assertThat(nat).isNotNull();
        assertThat(nat.size()).isEqualTo(1);
        assertThat(nat.get(0).getNatIdType()).isEqualTo(NaturalPersonId.NatIdType.PASSPORT_NUMBER);
        assertThat(nat.get(0).getNatId()).isEqualTo("ID");
        assertThat(nat.get(0).getNatIdCountry()).isEqualTo(Country.ALL.get("DE"));
        assertThat(nat.get(0).getNatIdIssuer()).isEqualTo("");
        
        assertThat(sessionRequest.getComment()).isNotNull();
        assertThat(sessionRequest.getComment()).isEmpty();
        
        JSONAssert.assertEquals(
                loadTestJson("session-request.json"),
                toJson(sessionRequest),
                false);
    }

    @Test
    @SneakyThrows
    public void sessionReply() {
        val sessionReply = loadTestJson(SessionReply.class, "session-reply.json");
        sessionReply.validate();
        
        val msg = sessionReply.getHeader();
        assertThat(msg).isNotNull();
        assertThat(msg.getMessageType()).isEqualTo(VaspMessage.TypeDescriptor.SESSION_REPLY);
        assertThat(msg.getMessageId()).isEqualTo("0x664ae93d657bd540a06664ed92d6f734");
        assertThat(msg.getSessionId()).isEqualTo("0x8e8667b04d7ef44b8ae5617b472a0108");
        assertThat(msg.getResponseCode()).isEqualTo("1");
        
        val handshake = sessionReply.getHandshake();
        assertThat(handshake).isNotNull();
        assertThat(handshake.getTopicB().getData()).isEqualTo("0x75ce1e35");
        
        val vasp = sessionReply.getVaspInfo();
        assertThat(vasp).isNotNull();
        assertThat(vasp.getName()).isEqualTo("TestOpenVaspJur");
        assertThat(vasp.getVaspId().toString()).isEqualTo("0x08FDa931D64b17c3aCFfb35C1B3902e0BBB4eE5C");
        assertThat(vasp.getPk()).isEqualTo("0x04d6d18d789fc1c61feb3c84342e8e4ff1ab650fcbafb29207f08cdad4755972483bbe24ff052a97ffb25c6b56139d64f2f6946bdf77807deefe3ab012dec359c1");
        
        val address = vasp.getAddress();
        assertThat(address).isNotNull();
        assertThat(address.getStreet()).isEqualTo("JurStreetName");
        assertThat(address.getNumber()).isEqualTo("124");
        assertThat(address.getAdrline()).isEqualTo("JurAddressLine");
        assertThat(address.getPostCode()).isEqualTo("2100000");
        assertThat(address.getTown()).isEqualTo("TownN-1");
        assertThat(address.getCountry()).isEqualTo(Country.ALL.get("DE"));
        
        val jur = vasp.getJur();
        assertThat(jur).isNotNull();
        assertThat(jur.size()).isEqualTo(1);
        assertThat(jur.get(0).getJurIdType()).isEqualTo(JuridicalPersonId.JurIdType.BANK_PARTY_IDENTIFICATION);
        assertThat(jur.get(0).getJurId()).isEqualTo("ID");
        assertThat(jur.get(0).getJurIdCountry()).isEqualTo(Country.ALL.get("DE"));
        assertThat(jur.get(0).getJurIdIssuer()).isEqualTo("");
        
        assertThat(sessionReply.getComment()).isNotNull();
        assertThat(sessionReply.getComment()).isEmpty();
        
        JSONAssert.assertEquals(
                loadTestJson("session-reply.json"),
                toJson(sessionReply),
                false);
    }

    @Test
    @SneakyThrows
    public void transferRequest() {
        val transferRequest = loadTestJson(TransferRequest.class, "transfer-request.json");
        transferRequest.validate();
        
        val msg = transferRequest.getHeader();
        assertThat(msg).isNotNull();
        assertThat(msg.getMessageType()).isEqualTo(VaspMessage.TypeDescriptor.TRANSFER_REQUEST);
        assertThat(msg.getMessageId()).isEqualTo("0xb9e111b5136bb244b674ec978c2c6444");
        assertThat(msg.getSessionId()).isEqualTo("0xa62eaebed74300448b9e6bd89a3a8f6e");
        assertThat(msg.getResponseCode()).isEqualTo("1");
        
        val originator = transferRequest.getOriginator();
        assertThat(originator).isNotNull();
        assertThat(originator.getName()).isEqualTo("Test van der Test");
        assertThat(originator.getVaan().getData()).isEqualTo("7dface61524ee3fb0828095d");
        
        val originatorAddress = originator.getAddress();
        assertThat(originatorAddress).isNotNull();
        assertThat(originatorAddress.getStreet()).isEqualTo("StreetX");
        assertThat(originatorAddress.getNumber()).isEqualTo("44");
        assertThat(originatorAddress.getAdrline()).isEqualTo("AddressLineX");
        assertThat(originatorAddress.getPostCode()).isEqualTo("510051");
        assertThat(originatorAddress.getTown()).isEqualTo("TownX");
        assertThat(originatorAddress.getCountry()).isEqualTo(Country.ALL.get("DE"));
        
        val originatorBirth = originator.getBirth();
        assertThat(originatorBirth).isNotNull();
        assertThat(originatorBirth.getBirthDate()).isEqualTo("1990-04-28");
        assertThat(originatorBirth.getBirthCity()).isEqualTo("TownX");
        assertThat(originatorBirth.getBirthCountry()).isEqualTo(Country.ALL.get("DE"));
        
        val originatorNat = originator.getNat();
        assertThat(originatorNat).isNotNull();
        assertThat(originatorNat.size()).isEqualTo(1);
        assertThat(originatorNat.get(0).getNatIdType()).isEqualTo(NaturalPersonId.NatIdType.NATIONAL_IDENTITY_NUMBER);
        assertThat(originatorNat.get(0).getNatId()).isEqualTo("Id");
        assertThat(originatorNat.get(0).getNatIdCountry()).isEqualTo(Country.ALL.get("DE"));
        assertThat(originatorNat.get(0).getNatIdIssuer()).isEqualTo("");
        
        val beneficiary = transferRequest.getBeneficiary();
        assertThat(beneficiary).isNotNull();
        assertThat(beneficiary.getName()).isEqualTo("name");
        val vaan = beneficiary.getVaan();
        assertThat(vaan.getData()).isEqualTo("BBB4eE5C524ee3fb08280970");
        assertThat(vaan.getCustomerNr()).isEqualTo("524ee3fb082809");
        assertThat(vaan.getCheckSum()).isEqualTo("70");
        val vaspCode = vaan.getVaspCode();
        assertThat(vaspCode.toString()).isEqualTo("BBB4eE5C");
        
        val transfer = transferRequest.getTransfer();
        assertThat(transfer).isNotNull();
        assertThat(transfer.getAssetType()).isEqualTo(TransferMessage.VirtualAssetType.BTC);
        assertThat(transfer.getTransferType()).isEqualTo(TransferMessage.TransferType.BLOCKCHAIN_TRANSFER);
        assertThat(transfer.getAmount()).isEqualTo(new BigDecimal("123.0"));
        
        val vasp = transferRequest.getVaspInfo();
        assertThat(vasp).isNotNull();
        assertThat(vasp.getName()).isEqualTo("TestVaspContractPerson");
        assertThat(vasp.getVaspId().toString()).isEqualTo("0x6befaf0656b953b188a0ee3bf3db03d07dface61");
        assertThat(vasp.getPk()).isEqualTo("0x043061dce78a75a970fe7ff297870023931983982cb8615bfd9fe72c52f0040b6bf5e1a6c15085170eac9c584b2bf72d6296949e7ed60caaccf0c4f6ec0d330a5d");
        
        val address = vasp.getAddress();
        assertThat(address).isNotNull();
        assertThat(address.getStreet()).isEqualTo("Some StreetName ");
        assertThat(address.getNumber()).isEqualTo("64");
        assertThat(address.getAdrline()).isEqualTo("Some AddressLine");
        assertThat(address.getPostCode()).isEqualTo("310031");
        assertThat(address.getTown()).isEqualTo("TownN");
        assertThat(address.getCountry()).isEqualTo(Country.ALL.get("DE"));
        
        val birth = vasp.getBirth();
        assertThat(birth).isNotNull();
        assertThat(birth.getBirthDate()).isEqualTo("2020-04-28");
        assertThat(birth.getBirthCity()).isEqualTo("Town X");
        assertThat(birth.getBirthCountry()).isEqualTo(Country.ALL.get("DE"));
        
        val nat = vasp.getNat();
        assertThat(nat).isNotNull();
        assertThat(nat.size()).isEqualTo(1);
        assertThat(nat.get(0).getNatIdType()).isEqualTo(NaturalPersonId.NatIdType.PASSPORT_NUMBER);
        assertThat(nat.get(0).getNatId()).isEqualTo("ID");
        assertThat(nat.get(0).getNatIdCountry()).isEqualTo(Country.ALL.get("DE"));
        assertThat(nat.get(0).getNatIdIssuer()).isEqualTo("");
        
        assertThat(transferRequest.getComment()).isNotNull();
        assertThat(transferRequest.getComment()).isEmpty();
        
        JSONAssert.assertEquals(
                loadTestJson("transfer-request.json"),
                toJson(transferRequest),
                false);
    }

    @Test
    @SneakyThrows
    public void transferReply() {
        val transferReply = loadTestJson(TransferRequest.class, "transfer-reply.json");
        transferReply.validate();
        
        val msg = transferReply.getHeader();
        assertThat(msg).isNotNull();
        assertThat(msg.getMessageType()).isEqualTo(VaspMessage.TypeDescriptor.TRANSFER_REPLY);
        assertThat(msg.getMessageId()).isEqualTo("0xb6e0e015ecc99e4b9ef70b44281df8f1");
        assertThat(msg.getSessionId()).isEqualTo("0x248d987c83e76548840f0f9d6032fd89");
        assertThat(msg.getResponseCode()).isEqualTo("1");
        
        val originator = transferReply.getOriginator();
        assertThat(originator).isNotNull();
        assertThat(originator.getName()).isEqualTo("Test van der Test");
        assertThat(originator.getVaan().getData()).isEqualTo("7dface61524ee3fb0828095d");
        
        val originatorAddress = originator.getAddress();
        assertThat(originatorAddress).isNotNull();
        assertThat(originatorAddress.getStreet()).isEqualTo("StreetX");
        assertThat(originatorAddress.getNumber()).isEqualTo("44");
        assertThat(originatorAddress.getAdrline()).isEqualTo("AddressLineX");
        assertThat(originatorAddress.getPostCode()).isEqualTo("510051");
        assertThat(originatorAddress.getTown()).isEqualTo("TownX");
        assertThat(originatorAddress.getCountry()).isEqualTo(Country.ALL.get("DE"));
        
        val originatorBirth = originator.getBirth();
        assertThat(originatorBirth).isNotNull();
        assertThat(originatorBirth.getBirthDate()).isEqualTo("1990-04-29");
        assertThat(originatorBirth.getBirthCity()).isEqualTo("TownX");
        assertThat(originatorBirth.getBirthCountry()).isEqualTo(Country.ALL.get("DE"));
        
        val originatorNat = originator.getNat();
        assertThat(originatorNat).isNotNull();
        assertThat(originatorNat.size()).isEqualTo(1);
        assertThat(originatorNat.get(0).getNatIdType()).isEqualTo(NaturalPersonId.NatIdType.NATIONAL_IDENTITY_NUMBER);
        assertThat(originatorNat.get(0).getNatId()).isEqualTo("Id");
        assertThat(originatorNat.get(0).getNatIdCountry()).isEqualTo(Country.ALL.get("DE"));
        assertThat(originatorNat.get(0).getNatIdIssuer()).isEqualTo("");
        
        val beneficiary = transferReply.getBeneficiary();
        assertThat(beneficiary).isNotNull();
        assertThat(beneficiary.getName()).isEqualTo("name");
        val vaan = beneficiary.getVaan();
        assertThat(vaan.getData()).isEqualTo("BBB4eE5C524ee3fb08280970");
        assertThat(vaan.getCustomerNr()).isEqualTo("524ee3fb082809");
        assertThat(vaan.getCheckSum()).isEqualTo("70");
        val vaspCode = vaan.getVaspCode();
        assertThat(vaspCode.toString()).isEqualTo("BBB4eE5C");
        
        val transfer = transferReply.getTransfer();
        assertThat(transfer).isNotNull();
        assertThat(transfer.getDestinationAddress()).isEqualTo("dest");
        assertThat(transfer.getAssetType()).isEqualTo(TransferMessage.VirtualAssetType.BTC);
        assertThat(transfer.getTransferType()).isEqualTo(TransferMessage.TransferType.BLOCKCHAIN_TRANSFER);
        assertThat(transfer.getAmount()).isEqualTo(new BigDecimal("123.0"));
        
        val vasp = transferReply.getVaspInfo();
        assertThat(vasp).isNotNull();
        assertThat(vasp.getName()).isEqualTo("TestOpenVaspJur");
        assertThat(vasp.getVaspId().toString()).isEqualTo("0x08FDa931D64b17c3aCFfb35C1B3902e0BBB4eE5C");
        assertThat(vasp.getPk()).isEqualTo("0x04d6d18d789fc1c61feb3c84342e8e4ff1ab650fcbafb29207f08cdad4755972483bbe24ff052a97ffb25c6b56139d64f2f6946bdf77807deefe3ab012dec359c1");
        
        val address = vasp.getAddress();
        assertThat(address).isNotNull();
        assertThat(address.getStreet()).isEqualTo("JurStreetName");
        assertThat(address.getNumber()).isEqualTo("124");
        assertThat(address.getAdrline()).isEqualTo("JurAddressLine");
        assertThat(address.getPostCode()).isEqualTo("2100000");
        assertThat(address.getTown()).isEqualTo("TownN-1");
        assertThat(address.getCountry()).isEqualTo(Country.ALL.get("DE"));
        
        val jur = vasp.getJur();
        assertThat(jur).isNotNull();
        assertThat(jur.size()).isEqualTo(1);
        assertThat(jur.get(0).getJurIdType()).isEqualTo(JuridicalPersonId.JurIdType.BANK_PARTY_IDENTIFICATION);
        assertThat(jur.get(0).getJurId()).isEqualTo("ID");
        assertThat(jur.get(0).getJurIdCountry()).isEqualTo(Country.ALL.get("DE"));
        assertThat(jur.get(0).getJurIdIssuer()).isEqualTo("");
        
        assertThat(transferReply.getComment()).isNotNull();
        assertThat(transferReply.getComment()).isEmpty();
        
        JSONAssert.assertEquals(
                loadTestJson("transfer-reply.json"),
                toJson(transferReply),
                false);
    }

    @Test
    @SneakyThrows
    public void transferDispatch() {
        val transferDispatch = loadTestJson(TransferDispatch.class, "transfer-dispatch.json");
        transferDispatch.validate();
        
        val msg = transferDispatch.getHeader();
        assertThat(msg).isNotNull();
        assertThat(msg.getMessageType()).isEqualTo(VaspMessage.TypeDescriptor.TRANSFER_DISPATCH);
        assertThat(msg.getMessageId()).isEqualTo("0x4419d87a87fbbb4daa0ad6a671f4bd6e");
        assertThat(msg.getSessionId()).isEqualTo("0xa62eaebed74300448b9e6bd89a3a8f6e");
        assertThat(msg.getResponseCode()).isEqualTo("1");
        
        val originator = transferDispatch.getOriginator();
        assertThat(originator).isNotNull();
        assertThat(originator.getName()).isEqualTo("Test van der Test");
        assertThat(originator.getVaan().getData()).isEqualTo("7dface61524ee3fb0828095d");

        val originatorAddress = originator.getAddress();
        assertThat(originatorAddress).isNotNull();
        assertThat(originatorAddress.getStreet()).isEqualTo("StreetX");
        assertThat(originatorAddress.getNumber()).isEqualTo("44");
        assertThat(originatorAddress.getAdrline()).isEqualTo("AddressLineX");
        assertThat(originatorAddress.getPostCode()).isEqualTo("510051");
        assertThat(originatorAddress.getTown()).isEqualTo("TownX");
        assertThat(originatorAddress.getCountry()).isEqualTo(Country.ALL.get("DE"));

        val originatorBirth = originator.getBirth();
        assertThat(originatorBirth).isNotNull();
        assertThat(originatorBirth.getBirthDate()).isEqualTo("1990-04-28");
        assertThat(originatorBirth.getBirthCity()).isEqualTo("TownX");
        assertThat(originatorBirth.getBirthCountry()).isEqualTo(Country.ALL.get("DE"));

        val originatorNat = originator.getNat();
        assertThat(originatorNat).isNotNull();
        assertThat(originatorNat.size()).isEqualTo(1);
        assertThat(originatorNat.get(0).getNatIdType()).isEqualTo(NaturalPersonId.NatIdType.NATIONAL_IDENTITY_NUMBER);
        assertThat(originatorNat.get(0).getNatId()).isEqualTo("Id");
        assertThat(originatorNat.get(0).getNatIdCountry()).isEqualTo(Country.ALL.get("DE"));
        assertThat(originatorNat.get(0).getNatIdIssuer()).isEqualTo("");
        
        val beneficiary = transferDispatch.getBeneficiary();
        assertThat(beneficiary).isNotNull();
        assertThat(beneficiary.getName()).isEqualTo("benef_name");
        val vaan = beneficiary.getVaan();
        assertThat(vaan.getData()).isEqualTo("BBB4eE5C524ee3fb08280970");
        assertThat(vaan.getCustomerNr()).isEqualTo("524ee3fb082809");
        assertThat(vaan.getCheckSum()).isEqualTo("70");
        val vaspCode = vaan.getVaspCode();
        assertThat(vaspCode.toString()).isEqualTo("BBB4eE5C");
        
        val transfer = transferDispatch.getTransfer();
        assertThat(transfer).isNotNull();
        assertThat(transfer.getDestinationAddress()).isEqualTo("dest");
        assertThat(transfer.getAssetType()).isEqualTo(TransferMessage.VirtualAssetType.BTC);
        assertThat(transfer.getTransferType()).isEqualTo(TransferMessage.TransferType.BLOCKCHAIN_TRANSFER);
        assertThat(transfer.getAmount()).isEqualTo(new BigDecimal("123.0"));
        
        val transaction = transferDispatch.getTx();
        assertThat(transaction).isNotNull();
        assertThat(transaction.getId()).isEqualTo("hash");
        assertThat(transaction.getDateTime()).isEqualTo(ZonedDateTime.parse("2020-04-28T06:07:36Z"));
        assertThat(transaction.getSendingAddress()).isEqualTo("sending_addr");
        
        val vasp = transferDispatch.getVaspInfo();
        assertThat(vasp).isNotNull();
        assertThat(vasp.getName()).isEqualTo("TestVaspContractPerson");
        assertThat(vasp.getVaspId().toString()).isEqualTo("0x6befaf0656b953b188a0ee3bf3db03d07dface61");
        assertThat(vasp.getPk()).isEqualTo("0x043061dce78a75a970fe7ff297870023931983982cb8615bfd9fe72c52f0040b6bf5e1a6c15085170eac9c584b2bf72d6296949e7ed60caaccf0c4f6ec0d330a5d");
        
        val address = vasp.getAddress();
        assertThat(address).isNotNull();
        assertThat(address.getStreet()).isEqualTo("Some StreetName ");
        assertThat(address.getNumber()).isEqualTo("64");
        assertThat(address.getAdrline()).isEqualTo("Some AddressLine");
        assertThat(address.getPostCode()).isEqualTo("310031");
        assertThat(address.getTown()).isEqualTo("TownN");
        assertThat(address.getCountry()).isEqualTo(Country.ALL.get("DE"));
        
        val birth = vasp.getBirth();
        assertThat(birth).isNotNull();
        assertThat(birth.getBirthDate()).isEqualTo("2020-04-28");
        assertThat(birth.getBirthCity()).isEqualTo("Town X");
        assertThat(birth.getBirthCountry()).isEqualTo(Country.ALL.get("DE"));
        
        val nat = vasp.getNat();
        assertThat(nat).isNotNull();
        assertThat(nat.size()).isEqualTo(1);
        assertThat(nat.get(0).getNatIdType()).isEqualTo(NaturalPersonId.NatIdType.PASSPORT_NUMBER);
        assertThat(nat.get(0).getNatId()).isEqualTo("ID");
        assertThat(nat.get(0).getNatIdCountry()).isEqualTo(Country.ALL.get("DE"));
        assertThat(nat.get(0).getNatIdIssuer()).isEqualTo("");
        
        assertThat(transferDispatch.getComment()).isNotNull();
        assertThat(transferDispatch.getComment()).isEmpty();
        
        JSONAssert.assertEquals(
                loadTestJson("transfer-dispatch.json"),
                toJson(transferDispatch),
                false);
        
    }

    @Test
    @SneakyThrows
    public void transferConfirmation() {
        val transferConfirmation = loadTestJson(TransferConfirmation.class, "transfer-confirmation.json");
        transferConfirmation.validate();
        
        val msg = transferConfirmation.getHeader();
        assertThat(msg).isNotNull();
        assertThat(msg.getMessageType()).isEqualTo(VaspMessage.TypeDescriptor.TRANSFER_CONFIRMATION);
        assertThat(msg.getMessageId()).isEqualTo("0x5cd76b215d304a4d86802928374a218c");
        assertThat(msg.getSessionId()).isEqualTo("0xf3490c64edf1e6439023a3f7f22ed718");
        assertThat(msg.getResponseCode()).isEqualTo("1");
        
        val originator = transferConfirmation.getOriginator();
        assertThat(originator).isNotNull();
        assertThat(originator.getName()).isEqualTo("Test van der Test");
        assertThat(originator.getVaan().getData()).isEqualTo("7dface61524ee3fb0828095d");
        
        val originatorAddress = originator.getAddress();
        assertThat(originatorAddress).isNotNull();
        assertThat(originatorAddress.getStreet()).isEqualTo("StreetX");
        assertThat(originatorAddress.getNumber()).isEqualTo("44");
        assertThat(originatorAddress.getAdrline()).isEqualTo("AddressLineX");
        assertThat(originatorAddress.getPostCode()).isEqualTo("510051");
        assertThat(originatorAddress.getTown()).isEqualTo("TownX");
        assertThat(originatorAddress.getCountry()).isEqualTo(Country.ALL.get("DE"));
        
        val originatorBirth = originator.getBirth();
        assertThat(originatorBirth).isNotNull();
        assertThat(originatorBirth.getBirthDate()).isEqualTo("1990-04-30");
        assertThat(originatorBirth.getBirthCity()).isEqualTo("TownX");
        assertThat(originatorBirth.getBirthCountry()).isEqualTo(Country.ALL.get("DE"));
        
        val originatorNat = originator.getNat();
        assertThat(originatorNat).isNotNull();
        assertThat(originatorNat.size()).isEqualTo(1);
        assertThat(originatorNat.get(0).getNatIdType()).isEqualTo(NaturalPersonId.NatIdType.NATIONAL_IDENTITY_NUMBER);
        assertThat(originatorNat.get(0).getNatId()).isEqualTo("Id");
        assertThat(originatorNat.get(0).getNatIdCountry()).isEqualTo(Country.ALL.get("DE"));
        assertThat(originatorNat.get(0).getNatIdIssuer()).isEqualTo("");
        
        val beneficiary = transferConfirmation.getBeneficiary();
        assertThat(beneficiary).isNotNull();
        assertThat(beneficiary.getName()).isEqualTo("name");
        val vaan = beneficiary.getVaan();
        assertThat(vaan.getData()).isEqualTo("BBB4eE5C524ee3fb08280970");
        assertThat(vaan.getCustomerNr()).isEqualTo("524ee3fb082809");
        assertThat(vaan.getCheckSum()).isEqualTo("70");
        val vaspCode = vaan.getVaspCode();
        assertThat(vaspCode.toString()).isEqualTo("BBB4eE5C");
        
        val transfer = transferConfirmation.getTransfer();
        assertThat(transfer).isNotNull();
        assertThat(transfer.getDestinationAddress()).isEqualTo("dest");
        assertThat(transfer.getAssetType()).isEqualTo(TransferMessage.VirtualAssetType.BTC);
        assertThat(transfer.getTransferType()).isEqualTo(TransferMessage.TransferType.BLOCKCHAIN_TRANSFER);
        assertThat(transfer.getAmount()).isEqualTo(new BigDecimal("123.0"));
        
        val transaction = transferConfirmation.getTx();
        assertThat(transaction).isNotNull();
        assertThat(transaction.getId()).isEqualTo("txid");
        assertThat(transaction.getDateTime()).isEqualTo(ZonedDateTime.parse("2020-04-30T06:35:36Z"));
        assertThat(transaction.getSendingAddress()).isEqualTo("sendingaddr");
        
        val vasp = transferConfirmation.getVaspInfo();
        assertThat(vasp).isNotNull();
        assertThat(vasp.getName()).isEqualTo("TestOpenVaspJur");
        assertThat(vasp.getVaspId().toString()).isEqualTo("0x08FDa931D64b17c3aCFfb35C1B3902e0BBB4eE5C");
        assertThat(vasp.getPk()).isEqualTo("0x04d6d18d789fc1c61feb3c84342e8e4ff1ab650fcbafb29207f08cdad4755972483bbe24ff052a97ffb25c6b56139d64f2f6946bdf77807deefe3ab012dec359c1");
        
        val address = vasp.getAddress();
        assertThat(address).isNotNull();
        assertThat(address.getStreet()).isEqualTo("JurStreetName");
        assertThat(address.getNumber()).isEqualTo("124");
        assertThat(address.getAdrline()).isEqualTo("JurAddressLine");
        assertThat(address.getPostCode()).isEqualTo("2100000");
        assertThat(address.getTown()).isEqualTo("TownN-1");
        assertThat(address.getCountry()).isEqualTo(Country.ALL.get("DE"));
        
        val jur = vasp.getJur();
        assertThat(jur).isNotNull();
        assertThat(jur.size()).isEqualTo(1);
        assertThat(jur.get(0).getJurIdType()).isEqualTo(JuridicalPersonId.JurIdType.BANK_PARTY_IDENTIFICATION);
        assertThat(jur.get(0).getJurId()).isEqualTo("ID");
        assertThat(jur.get(0).getJurIdCountry()).isEqualTo(Country.ALL.get("DE"));
        assertThat(jur.get(0).getJurIdIssuer()).isEqualTo("");
        
        assertThat(transferConfirmation.getComment()).isNotNull();
        assertThat(transferConfirmation.getComment()).isEmpty();
        
        JSONAssert.assertEquals(
                loadTestJson("transfer-confirmation.json"),
                toJson(transferConfirmation),
                false);
    }

    @Test
    @SneakyThrows
    public void termination() {
        val termination = loadTestJson(TerminationMessage.class, "termination.json");
        termination.validate();
        
        val msg = termination.getHeader();
        assertThat(msg).isNotNull();
        assertThat(msg.getMessageType()).isEqualTo(VaspMessage.TypeDescriptor.TERMINATION);
        assertThat(msg.getMessageId()).isEqualTo("0x5868b9808c46ce4b88884b44611198b6");
        assertThat(msg.getSessionId()).isEqualTo("0xa62eaebed74300448b9e6bd89a3a8f6e");
        assertThat(msg.getResponseCode()).isEqualTo("1");
        
        val vasp = termination.getVaspInfo();
        assertThat(vasp).isNotNull();
        assertThat(vasp.getName()).isEqualTo("TestVaspContractPerson");
        assertThat(vasp.getVaspId().toString()).isEqualTo("0x6befaf0656b953b188a0ee3bf3db03d07dface61");
        assertThat(vasp.getPk()).isEqualTo("0x043061dce78a75a970fe7ff297870023931983982cb8615bfd9fe72c52f0040b6bf5e1a6c15085170eac9c584b2bf72d6296949e7ed60caaccf0c4f6ec0d330a5d");
        
        val address = vasp.getAddress();
        assertThat(address).isNotNull();
        assertThat(address.getStreet()).isEqualTo("Some StreetName ");
        assertThat(address.getNumber()).isEqualTo("64");
        assertThat(address.getAdrline()).isEqualTo("Some AddressLine");
        assertThat(address.getPostCode()).isEqualTo("310031");
        assertThat(address.getTown()).isEqualTo("TownN");
        assertThat(address.getCountry()).isEqualTo(Country.ALL.get("DE"));
        
        val birth = vasp.getBirth();
        assertThat(birth).isNotNull();
        assertThat(birth.getBirthDate()).isEqualTo("2020-04-28");
        assertThat(birth.getBirthCity()).isEqualTo("Town X");
        assertThat(birth.getBirthCountry()).isEqualTo(Country.ALL.get("DE"));
        
        val nat = vasp.getNat();
        assertThat(nat).isNotNull();
        assertThat(nat.size()).isEqualTo(1);
        assertThat(nat.get(0).getNatIdType()).isEqualTo(NaturalPersonId.NatIdType.PASSPORT_NUMBER);
        assertThat(nat.get(0).getNatId()).isEqualTo("ID");
        assertThat(nat.get(0).getNatIdCountry()).isEqualTo(Country.ALL.get("DE"));
        assertThat(nat.get(0).getNatIdIssuer()).isEqualTo("");
        
        assertThat(termination.getComment()).isNotNull();
        assertThat(termination.getComment()).isEmpty();
        
        JSONAssert.assertEquals(
                loadTestJson("termination.json"),
                toJson(termination),
                false);
    }
}
