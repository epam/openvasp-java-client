package org.openvasp.client.model;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.openvasp.client.common.JsonPathFixture;
import org.skyscreamer.jsonassert.JSONAssert;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openvasp.client.common.Json.loadTestJson;
import static org.openvasp.client.common.Json.toJson;
import static org.openvasp.client.model.VaspMessage.TypeDescriptor;


/**
 * @author Olexandr_Bilovol@epam.com
 */
class JsonSer2Tests {

    private static final String SER_DATA_FOLDER = "serialization/messages";

    private final DateTimeFormatter birthDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Test
    @SneakyThrows
    void sessionRequest() {
        var jsonFileName = SER_DATA_FOLDER + "/session-request.json";
        var sessionRequest = loadTestJson(SessionRequest.class, jsonFileName);
        var jsonFixture = new JsonPathFixture(loadTestJson(jsonFileName));

        sessionRequest.validate();
        checkBaseMessage(sessionRequest, TypeDescriptor.SESSION_REQUEST, jsonFixture);
        checkNaturalPerson(sessionRequest, jsonFixture);
        checkVaspInfo(sessionRequest, TypeDescriptor.SESSION_REQUEST, jsonFixture);

        var handshake = sessionRequest.getHandshake();
        assertThat(handshake).isNotNull();
        jsonFixture.assertEquals(handshake.getTopicA().getData(), "$.handshake.topica");
        jsonFixture.assertEquals(handshake.getSessionPublicKey(), "$.handshake.ecdhpk");

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(sessionRequest), false);
    }

    @Test
    @SneakyThrows
    void sessionReply() {
        var jsonFileName = SER_DATA_FOLDER + "/session-reply.json";
        var sessionReply = loadTestJson(SessionReply.class, jsonFileName);
        var jsonFixture = new JsonPathFixture(loadTestJson(jsonFileName));

        sessionReply.validate();
        checkBaseMessage(sessionReply, TypeDescriptor.SESSION_REPLY, jsonFixture);

        var handshake = sessionReply.getHandshake();
        assertThat(handshake).isNotNull();
        jsonFixture.assertEquals(handshake.getTopicB().getData(), "$.handshake.topicb");

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(sessionReply), false);
    }

    @Test
    @SneakyThrows
    void transferRequest() {
        var jsonFileName = SER_DATA_FOLDER + "/transfer-request.json";
        var transferRequest = loadTestJson(TransferRequest.class, jsonFileName);
        var jsonFixture = new JsonPathFixture(loadTestJson(jsonFileName));

        transferRequest.validate();
        checkBaseMessage(transferRequest, TypeDescriptor.TRANSFER_REQUEST, jsonFixture);
        checkOriginator(transferRequest, jsonFixture);
        checkBeneficiary(transferRequest, jsonFixture);

        var transfer = transferRequest.getTransfer();
        assertThat(transfer).isNotNull();
        assertThat(transfer.getAssetType()).isEqualTo(TransferRequest.VirtualAssetType.BTC);
        assertThat(transfer.getTransferType()).isEqualTo(TransferRequest.TransferType.BLOCKCHAIN_TRANSFER);
        assertThat(transfer.getAmount()).isEqualTo(new BigDecimal("123.0"));

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(transferRequest), false);
    }

    @Test
    @SneakyThrows
    void transferReply() {
        var jsonFileName = SER_DATA_FOLDER + "/transfer-reply.json";
        var transferReply = loadTestJson(TransferReply.class, jsonFileName);
        var jsonFixture = new JsonPathFixture(loadTestJson(jsonFileName));

        transferReply.validate();
        checkBaseMessage(transferReply, TypeDescriptor.TRANSFER_REPLY, jsonFixture);
        assertThat(transferReply.getDestinationAddress()).isEqualTo("destinationAddress");

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(transferReply), false);
    }

    @Test
    @SneakyThrows
    void transferDispatch() {
        var jsonFileName = SER_DATA_FOLDER + "/transfer-dispatch.json";
        var transferDispatch = loadTestJson(TransferDispatch.class, jsonFileName);
        var jsonFixture = new JsonPathFixture(loadTestJson(jsonFileName));

        transferDispatch.validate();
        checkBaseMessage(transferDispatch, TypeDescriptor.TRANSFER_DISPATCH, jsonFixture);

        var transaction = transferDispatch.getTx();
        assertThat(transaction).isNotNull();
        assertThat(transaction.getId()).isEqualTo("hash");
        assertThat(transaction.getDateTime()).isEqualTo(ZonedDateTime.parse("2020-06-08T09:36:29Z"));
        assertThat(transaction.getSendingAddress()).isEqualTo("sending_addr");

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(transferDispatch), false);
    }

    @Test
    @SneakyThrows
    void transferConfirmation() {
        var jsonFileName = SER_DATA_FOLDER + "/transfer-confirmation.json";
        var transferConfirmation = loadTestJson(TransferConfirmation.class, jsonFileName);
        var jsonFixture = new JsonPathFixture(loadTestJson(jsonFileName));

        transferConfirmation.validate();
        checkBaseMessage(transferConfirmation, TypeDescriptor.TRANSFER_CONFIRMATION, jsonFixture);

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(transferConfirmation), false);
    }

    @Test
    @SneakyThrows
    void termination() {
        var jsonFileName = SER_DATA_FOLDER + "/termination.json";
        var termination = loadTestJson(TerminationMessage.class, jsonFileName);
        var jsonFixture = new JsonPathFixture(loadTestJson(jsonFileName));

        termination.validate();
        checkBaseMessage(termination, TypeDescriptor.TERMINATION, jsonFixture);

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(termination), false);
    }

    private void checkBaseMessage(
            final VaspMessage message,
            final TypeDescriptor expectedType,
            final JsonPathFixture jsonFixture) {

        var msg = message.getHeader();
        assertThat(msg).isNotNull();
        assertThat(msg.getMessageType()).isEqualTo(expectedType);
        jsonFixture.assertEquals(msg.getMessageId(), "$.msg.msgid");
        jsonFixture.assertEquals(msg.getSessionId(), "$.msg.session");
        assertThat(msg.getResponseCode()).isEqualTo("1");

        assertThat(message.getComment()).isNotNull();
        assertThat(message.getComment()).isEmpty();
    }

    private void checkVaspInfo(
            final SessionMessage message,
            final TypeDescriptor expectedType,
            final JsonPathFixture jsonFixture) {

        var vasp = message.getVaspInfo();
        assertThat(vasp).isNotNull();
        jsonFixture.assertEquals(vasp.getName(), "$.vasp.name");
        jsonFixture.assertEquals(vasp.getVaspId().toString(), "$.vasp.id");
        jsonFixture.assertEquals(vasp.getPk(), "$.vasp.pk");

        var address = vasp.getAddress();
        assertThat(address).isNotNull();
        jsonFixture.assertEquals(address.getStreet(), "$.vasp.address.street");
        jsonFixture.assertEquals(address.getNumber(), "$.vasp.address.number");
        jsonFixture.assertEquals(address.getAdrline(), "$.vasp.address.adrline");
        jsonFixture.assertEquals(address.getPostCode(), "$.vasp.address.postcode");
        jsonFixture.assertEquals(address.getTown(), "$.vasp.address.town");
        jsonFixture.assertEquals(address.getCountry().getCode(), "$.vasp.address.country");
    }

    private void checkNaturalPerson(
            final SessionMessage message,
            final JsonPathFixture jsonFixture) {

        var birth = message.getVaspInfo().getBirth();
        assertThat(birth).isNotNull();
        jsonFixture.assertEquals(birthDateFormat.format(birth.getBirthDate()), "$.vasp.birth.birthdate");
        jsonFixture.assertEquals(birth.getBirthCity(), "$.vasp.birth.birthcity");
        jsonFixture.assertEquals(birth.getBirthCountry().getCode(), "$.vasp.birth.birthcountry");

        var nat = message.getVaspInfo().getNat();
        assertThat(nat).isNotNull();
        assertThat(nat.size()).isEqualTo(1);
        assertThat(nat.get(0).getNatIdType()).isEqualTo(NaturalPersonId.NatIdType.PASSPORT_NUMBER);
        assertThat(nat.get(0).getNatId()).isEqualTo("ID");
        assertThat(nat.get(0).getNatIdCountry()).isEqualTo(Country.ALL.get("DE"));
        assertThat(nat.get(0).getNatIdIssuer()).isEmpty();
    }

    @SuppressWarnings("unused")
    private void checkJuridicalPerson(
            final SessionMessage message,
            final JsonPathFixture jsonFixture) {

        var jur = message.getVaspInfo().getJur();
        assertThat(jur).isNotNull();
        assertThat(jur.size()).isEqualTo(1);
        assertThat(jur.get(0).getJurIdType()).isEqualTo(JuridicalPersonId.JurIdType.BANK_PARTY_IDENTIFICATION);
        assertThat(jur.get(0).getJurId()).isEqualTo("ID");
        assertThat(jur.get(0).getJurIdCountry()).isEqualTo(Country.ALL.get("DE"));
        assertThat(jur.get(0).getJurIdIssuer()).isEmpty();
    }

    private void checkOriginator(
            final TransferRequest message,
            final JsonPathFixture jsonFixture) {

        var originator = message.getOriginator();
        assertThat(originator).isNotNull();
        assertThat(originator.getName()).isEqualTo("Test van der Test");
        jsonFixture.assertEquals(originator.getVaan().getData(), "$.originator.vaan");

        var originatorAddress = originator.getAddress();
        assertThat(originatorAddress).isNotNull();
        assertThat(originatorAddress.getStreet()).isEqualTo("StreetX");
        assertThat(originatorAddress.getNumber()).isEqualTo("44");
        assertThat(originatorAddress.getAdrline()).isEqualTo("AddressLineX");
        assertThat(originatorAddress.getPostCode()).isEqualTo("510051");
        assertThat(originatorAddress.getTown()).isEqualTo("TownX");
        assertThat(originatorAddress.getCountry()).isEqualTo(Country.ALL.get("DE"));

        var originatorBirth = originator.getBirth();
        assertThat(originatorBirth).isNotNull();
        assertThat(originatorBirth.getBirthDate()).isEqualTo("1990-06-08");
        assertThat(originatorBirth.getBirthCity()).isEqualTo("TownX");
        assertThat(originatorBirth.getBirthCountry()).isEqualTo(Country.ALL.get("DE"));

        var originatorNat = originator.getNat();
        assertThat(originatorNat).isNotNull();
        assertThat(originatorNat.size()).isEqualTo(1);
        assertThat(originatorNat.get(0).getNatIdType()).isEqualTo(NaturalPersonId.NatIdType.NATIONAL_IDENTITY_NUMBER);
        assertThat(originatorNat.get(0).getNatId()).isEqualTo("Id");
        assertThat(originatorNat.get(0).getNatIdCountry()).isEqualTo(Country.ALL.get("DE"));
        assertThat(originatorNat.get(0).getNatIdIssuer()).isEmpty();
    }

    private void checkBeneficiary(
            final TransferRequest message,
            final JsonPathFixture jsonFixture) {

        var beneficiary = message.getBeneficiary();
        assertThat(beneficiary).isNotNull();
        assertThat(beneficiary.getName()).isEqualTo("name");
        var vaan = beneficiary.getVaan();
        assertThat(vaan.getData()).isEqualTo("1000BBB4eE5Ce3fb082809e0");
        assertThat(vaan.getCustomerNr()).isEqualTo("e3fb082809");
        assertThat(vaan.getCheckSum()).isEqualTo("e0");
        var vaspCode = vaan.getVaspCode();
        assertThat(vaspCode).hasToString("BBB4eE5C");
        var vaspCodeType = vaan.getVaspCodeType();
        assertThat(vaspCodeType).isEqualTo("10");
    }
}
