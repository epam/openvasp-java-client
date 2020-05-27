package org.openvasp.client.model;

import lombok.SneakyThrows;
import lombok.val;
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
public class JsonSer2Tests {

    private static final String SER_DATA_FOLDER = "serialization/messages";

    private final DateTimeFormatter birthDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Test
    @SneakyThrows
    public void sessionRequest() {
        val jsonFileName = SER_DATA_FOLDER + "/session-request.json";
        val sessionRequest = loadTestJson(SessionRequest.class, jsonFileName);
        val jsonFixture = new JsonPathFixture(loadTestJson(jsonFileName));

        sessionRequest.validate();
        checkBaseMessage(sessionRequest, TypeDescriptor.SESSION_REQUEST, jsonFixture);
        checkNaturalPerson(sessionRequest, jsonFixture);

        val handshake = sessionRequest.getHandshake();
        assertThat(handshake).isNotNull();
        jsonFixture.assertEquals(handshake.getTopicA().getData(), "$.handshake.topica");
        jsonFixture.assertEquals(handshake.getSessionPublicKey(), "$.handshake.ecdhpk");

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(sessionRequest), false);
    }

    @Test
    @SneakyThrows
    public void sessionReply() {
        val jsonFileName = SER_DATA_FOLDER + "/session-reply.json";
        val sessionReply = loadTestJson(SessionReply.class, jsonFileName);
        val jsonFixture = new JsonPathFixture(loadTestJson(jsonFileName));

        sessionReply.validate();
        checkBaseMessage(sessionReply, TypeDescriptor.SESSION_REPLY, jsonFixture);
        checkJuridicalPerson(sessionReply);

        val handshake = sessionReply.getHandshake();
        assertThat(handshake).isNotNull();
        jsonFixture.assertEquals(handshake.getTopicB().getData(), "$.handshake.topicb");

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(sessionReply), false);
    }

    @Test
    @SneakyThrows
    public void transferRequest() {
        val jsonFileName = SER_DATA_FOLDER + "/transfer-request.json";
        val transferRequest = loadTestJson(TransferRequest.class, jsonFileName);
        val jsonFixture = new JsonPathFixture(loadTestJson(jsonFileName));

        transferRequest.validate();
        checkBaseMessage(transferRequest, TypeDescriptor.TRANSFER_REQUEST, jsonFixture);
        checkNaturalPerson(transferRequest, jsonFixture);
        checkOriginator(transferRequest, jsonFixture);
        checkBenificiary(transferRequest, jsonFixture);

        val transfer = transferRequest.getTransfer();
        assertThat(transfer).isNotNull();
        assertThat(transfer.getAssetType()).isEqualTo(TransferMessage.VirtualAssetType.BTC);
        assertThat(transfer.getTransferType()).isEqualTo(TransferMessage.TransferType.BLOCKCHAIN_TRANSFER);
        assertThat(transfer.getAmount()).isEqualTo(new BigDecimal("123.0"));

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(transferRequest), false);
    }

    @Test
    @SneakyThrows
    public void transferReply() {
        val jsonFileName = SER_DATA_FOLDER + "/transfer-reply.json";
        val transferReply = loadTestJson(TransferRequest.class, jsonFileName);
        val jsonFixture = new JsonPathFixture(loadTestJson(jsonFileName));

        transferReply.validate();
        checkBaseMessage(transferReply, TypeDescriptor.TRANSFER_REPLY, jsonFixture);
        checkJuridicalPerson(transferReply);
        checkOriginator(transferReply, jsonFixture);
        checkBenificiary(transferReply, jsonFixture);

        val transfer = transferReply.getTransfer();
        assertThat(transfer).isNotNull();
        assertThat(transfer.getDestinationAddress()).isEqualTo("dest");
        assertThat(transfer.getAssetType()).isEqualTo(TransferMessage.VirtualAssetType.BTC);
        assertThat(transfer.getTransferType()).isEqualTo(TransferMessage.TransferType.BLOCKCHAIN_TRANSFER);
        assertThat(transfer.getAmount()).isEqualTo(new BigDecimal("123.0"));

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(transferReply), false);
    }

    @Test
    @SneakyThrows
    public void transferDispatch() {
        val jsonFileName = SER_DATA_FOLDER + "/transfer-dispatch.json";
        val transferDispatch = loadTestJson(TransferDispatch.class, jsonFileName);
        val jsonFixture = new JsonPathFixture(loadTestJson(jsonFileName));

        transferDispatch.validate();
        checkBaseMessage(transferDispatch, TypeDescriptor.TRANSFER_DISPATCH, jsonFixture);
        checkNaturalPerson(transferDispatch, jsonFixture);
        checkOriginator(transferDispatch, jsonFixture);
        checkBenificiary(transferDispatch, jsonFixture);

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

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(transferDispatch), false);
    }

    @Test
    @SneakyThrows
    public void transferConfirmation() {
        val jsonFileName = SER_DATA_FOLDER + "/transfer-confirmation.json";
        val transferConfirmation = loadTestJson(TransferConfirmation.class, jsonFileName);
        val jsonFixture = new JsonPathFixture(loadTestJson(jsonFileName));

        transferConfirmation.validate();
        checkBaseMessage(transferConfirmation, TypeDescriptor.TRANSFER_CONFIRMATION, jsonFixture);
        checkJuridicalPerson(transferConfirmation);
        checkOriginator(transferConfirmation, jsonFixture);
        checkBenificiary(transferConfirmation, jsonFixture);

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

        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(transferConfirmation), false);
    }

    @Test
    @SneakyThrows
    public void termination() {
        val jsonFileName = SER_DATA_FOLDER + "/termination.json";
        val termination = loadTestJson(TerminationMessage.class, jsonFileName);
        val jsonFixture = new JsonPathFixture(loadTestJson(jsonFileName));

        termination.validate();
        checkBaseMessage(termination, TypeDescriptor.TERMINATION, jsonFixture);
        checkNaturalPerson(termination, jsonFixture);
        JSONAssert.assertEquals(loadTestJson(jsonFileName), toJson(termination), false);
    }

    private void checkBaseMessage(
            final VaspMessage message,
            final TypeDescriptor expectedType,
            final JsonPathFixture jsonFixture) {

        val msg = message.getHeader();
        assertThat(msg).isNotNull();
        assertThat(msg.getMessageType()).isEqualTo(expectedType);
        jsonFixture.assertEquals(msg.getMessageId(), "$.msg.msgid");
        jsonFixture.assertEquals(msg.getSessionId(), "$.msg.session");
        assertThat(msg.getResponseCode()).isEqualTo("1");

        val vasp = message.getVaspInfo();
        assertThat(vasp).isNotNull();
        jsonFixture.assertEquals(vasp.getName(), "$.vasp.name");
        jsonFixture.assertEquals(vasp.getVaspId().toString(), "$.vasp.id");
        jsonFixture.assertEquals(vasp.getPk(), "$.vasp.pk");

        val address = vasp.getAddress();
        assertThat(address).isNotNull();
        jsonFixture.assertEquals(address.getStreet(), "$.vasp.address.street");
        jsonFixture.assertEquals(address.getNumber(), "$.vasp.address.number");
        jsonFixture.assertEquals(address.getAdrline(), "$.vasp.address.adrline");
        jsonFixture.assertEquals(address.getPostCode(), "$.vasp.address.postcode");
        jsonFixture.assertEquals(address.getTown(), "$.vasp.address.town");
        jsonFixture.assertEquals(address.getCountry().getCode(), "$.vasp.address.country");

        assertThat(message.getComment()).isNotNull();
        assertThat(message.getComment()).isEmpty();
    }

    private void checkNaturalPerson(
            final VaspMessage message,
            final JsonPathFixture jsonFixture) {

        val birth = message.getVaspInfo().getBirth();
        assertThat(birth).isNotNull();
        jsonFixture.assertEquals(birthDateFormat.format(birth.getBirthDate()), "$.vasp.birth.birthdate");
        jsonFixture.assertEquals(birth.getBirthCity(), "$.vasp.birth.birthcity");
        jsonFixture.assertEquals(birth.getBirthCountry().getCode(), "$.vasp.birth.birthcountry");

        val nat = message.getVaspInfo().getNat();
        assertThat(nat).isNotNull();
        assertThat(nat.size()).isEqualTo(1);
        assertThat(nat.get(0).getNatIdType()).isEqualTo(NaturalPersonId.NatIdType.PASSPORT_NUMBER);
        assertThat(nat.get(0).getNatId()).isEqualTo("ID");
        assertThat(nat.get(0).getNatIdCountry()).isEqualTo(Country.ALL.get("DE"));
        assertThat(nat.get(0).getNatIdIssuer()).isEqualTo("");
    }

    private void checkJuridicalPerson(
            final VaspMessage message) {

        val jur = message.getVaspInfo().getJur();
        assertThat(jur).isNotNull();
        assertThat(jur.size()).isEqualTo(1);
        assertThat(jur.get(0).getJurIdType()).isEqualTo(JuridicalPersonId.JurIdType.BANK_PARTY_IDENTIFICATION);
        assertThat(jur.get(0).getJurId()).isEqualTo("ID");
        assertThat(jur.get(0).getJurIdCountry()).isEqualTo(Country.ALL.get("DE"));
        assertThat(jur.get(0).getJurIdIssuer()).isEqualTo("");
    }

    private void checkOriginator(
            final TransferMessage message,
            final JsonPathFixture jsonFixture) {

        val originator = message.getOriginator();
        assertThat(originator).isNotNull();
        assertThat(originator.getName()).isEqualTo("Test van der Test");
        jsonFixture.assertEquals(originator.getVaan().getData(), "$.originator.vaan");

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
    }

    private void checkBenificiary(
            final TransferMessage message,
            final JsonPathFixture jsonFixture) {

        val beneficiary = message.getBeneficiary();
        assertThat(beneficiary).isNotNull();
        assertThat(beneficiary.getName()).isEqualTo("name");
        val vaan = beneficiary.getVaan();
        assertThat(vaan.getData()).isEqualTo("bbb4ee5c524ee3fb08280970");
        assertThat(vaan.getCustomerNr()).isEqualTo("524ee3fb082809");
        assertThat(vaan.getCheckSum()).isEqualTo("70");
        val vaspCode = vaan.getVaspCode();
        assertThat(vaspCode.toString()).isEqualTo("bbb4ee5c");
    }

}
