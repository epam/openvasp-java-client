package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.openvasp.client.common.Json;
import org.openvasp.client.common.VaspException;
import org.openvasp.client.common.VaspUtils;
import org.openvasp.client.common.VaspValidationException;
import org.web3j.utils.Numeric;

import java.util.List;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
@ToString(of = {"header"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class VaspMessage {

    @JsonProperty("msg")
    private Header header = new Header();

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("vasp")
    private VaspInfo vaspInfo;

    @JsonIgnore
    public final String getResponseCode() {
        return getHeader().responseCode;
    }

    @JsonIgnore
    public final VaspCode getSenderVaspCode() {
        checkState(vaspInfo != null);
        checkState(vaspInfo.getVaspId() != null);
        return vaspInfo.getVaspCode();
    }

    @JsonIgnore
    public Topic getConfirmationTopic() {
        checkState(header != null);
        checkState(header.messageId != null);
        return new Topic("0x" + StringUtils.right(header.messageId, 8));
    }

    public void validate() {
        header.validate(this);
        validateNotNull(vaspInfo, "vasp");
        validateNotNull(vaspInfo.getName(), "vasp.name");
        validateNotNull(vaspInfo.getVaspId(), "vasp.id");
        validateNotNull(vaspInfo.getPk(), "vasp.pk");
        validateNotNull(vaspInfo.getAddress(), "vasp.address");
        vaspInfo.validate(this);
    }

    void validateNotNull(final Object obj, @NonNull final String path) {
        if (obj == null) {
            throw new VaspValidationException(
                    this,
                    "%s.%s must not be null",
                    getClass().getSimpleName(),
                    path);
        }
    }

    public final SessionRequest asSessionRequest() {
        return downcast(TypeDescriptor.SESSION_REQUEST, SessionRequest.class);
    }

    public final SessionReply asSessionReply() {
        return downcast(TypeDescriptor.SESSION_REPLY, SessionReply.class);
    }

    public final TransferRequest asTransferRequest() {
        return downcast(TypeDescriptor.TRANSFER_REQUEST, TransferRequest.class);
    }

    public final TransferReply asTransferReply() {
        return downcast(TypeDescriptor.TRANSFER_REPLY, TransferReply.class);
    }

    public final TransferDispatch asTransferDispatch() {
        return downcast(TypeDescriptor.TRANSFER_DISPATCH, TransferDispatch.class);
    }

    public final TransferConfirmation asTransferConfirmation() {
        return downcast(TypeDescriptor.TRANSFER_CONFIRMATION, TransferConfirmation.class);
    }

    public final TerminationMessage asTerminationMessage() {
        return downcast(TypeDescriptor.TERMINATION, TerminationMessage.class);
    }

    public enum TypeDescriptor {

        SESSION_REQUEST(110),
        SESSION_REPLY(150),
        TRANSFER_REQUEST(210),
        TRANSFER_REPLY(250),
        TRANSFER_DISPATCH(310),
        TRANSFER_CONFIRMATION(350),
        TERMINATION(910);

        public final int id;

        TypeDescriptor(int id) {
            this.id = id;
        }

        @JsonValue
        public String getIdStr() {
            return String.valueOf(id);
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static TypeDescriptor fromIdStr(final String idStr) {
            int id = Integer.parseInt(idStr);
            for (val item : TypeDescriptor.values()) {
                if (item.id == id) {
                    return item;
                }
            }

            throw new NoSuchElementException(String.format(
                    "%s with id = %d does not exist",
                    TypeDescriptor.class.getName(),
                    id));
        }

    }

    @Getter
    @Setter
    @ToString(of = {"messageType", "messageId"})
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Header {

        public static final int MSG_ID_LENGTH = 32; // Hex(128-bit)
        public static final int SESSION_ID_LENGTH = 32; // Hex(128-bit)

        @JsonProperty("type")
        private TypeDescriptor messageType;

        @JsonProperty("msgid")
        private String messageId;

        @JsonProperty("session")
        private String sessionId;

        @JsonProperty("code")
        private String responseCode = VaspResponseCode.OK.id;

        public void validate(@NonNull final VaspMessage source) {
            if (Numeric.cleanHexPrefix(messageId).length() != MSG_ID_LENGTH || !VaspUtils.isValidHex(messageId)) {
                throw new VaspValidationException(source,
                        "The field 'msgid' is invalid - must be a hexadecimal string of length %d, but is: %s",
                        MSG_ID_LENGTH,
                        messageId);
            }

            if (Numeric.cleanHexPrefix(sessionId).length() != SESSION_ID_LENGTH || !VaspUtils.isValidHex(sessionId)) {
                throw new VaspValidationException(source,
                        "The field 'session' is invalid - must be a hexadecimal string of length %d, but is: %s",
                        SESSION_ID_LENGTH,
                        sessionId);
            }
        }

    }

    public static void checkRules(
            @NonNull final VaspMessage source,
            final BirthInfo birth,
            final List<NaturalPersonId> nat,
            final List<JuridicalPersonId> jur,
            final String bic) {

        if (birth != null || (nat != null && !nat.isEmpty())) {
            // Natural person
            if (StringUtils.isNotEmpty(bic)) {
                throw new VaspValidationException(source,
                        "[bic] must be empty for Natural Person");
            }
            if (jur != null && !jur.isEmpty()) {
                throw new VaspValidationException(source,
                        "[jur] must be empty for Natural Person");
            }
            if (nat != null) {
                for (val natPerson : nat) {
                    natPerson.validate(source);
                }
            }
        } else if (jur != null) {
            // Juridical person
            if (StringUtils.isNotEmpty(bic)) {
                throw new VaspValidationException(source,
                        "[bic] must be empty for Juridical Person");
            }
            for (JuridicalPersonId jurPerson : jur) {
                jurPerson.validate(source);
            }
        } else {
            // Bank
            if (StringUtils.isEmpty(bic)) {
                throw new VaspValidationException(source,
                        "[bic] must be present for Bank");
            }
        }
    }

    @SneakyThrows
    public static VaspMessage fromJson(@NonNull final String str) {
        val rootNode = Json.readTree(str);
        val messageTypeStr = extractMessageTypeStr(rootNode);
        val messageType = TypeDescriptor.fromIdStr(messageTypeStr);
        final VaspMessage result;
        switch (messageType) {
            case SESSION_REQUEST:
                result = Json.convertValue(SessionRequest.class, rootNode);
                break;
            case SESSION_REPLY:
                result = Json.convertValue(SessionReply.class, rootNode);
                break;
            case TRANSFER_REQUEST:
                result = Json.convertValue(TransferRequest.class, rootNode);
                break;
            case TRANSFER_REPLY:
                result = Json.convertValue(TransferReply.class, rootNode);
                break;
            case TRANSFER_DISPATCH:
                result = Json.convertValue(TransferDispatch.class, rootNode);
                break;
            case TRANSFER_CONFIRMATION:
                result = Json.convertValue(TransferConfirmation.class, rootNode);
                break;
            case TERMINATION:
                result = Json.convertValue(TerminationMessage.class, rootNode);
                break;
            default:
                throw new VaspException("An unknown message type " + messageTypeStr);
        }
        return result;
    }

    private static String extractMessageTypeStr(final JsonNode rootNode) {
        val msgNode = rootNode.findValue("msg");
        if (msgNode == null) {
            throw new VaspException("The field 'msg' of a VASP message cannot be null");
        }

        val msgTypeNode = msgNode.findValue("type");
        if (msgTypeNode == null) {
            throw new VaspException("The field 'msg.type' of a VASP message cannot be null");
        }

        val msgTypeStr = msgTypeNode.asText();
        if (msgTypeStr == null) {
            throw new VaspException("The field 'msg.type' of a VASP message cannot be null");
        }

        return msgTypeStr;
    }

    private <T> T downcast(final TypeDescriptor type, final Class<T> clazz) {
        checkState(header != null);
        checkState(header.messageType != null);

        if (!type.equals(header.messageType)) {
            throw new VaspException(
                    "Cannot cast the message of type '%s' to class %s",
                    header.messageType.getIdStr(),
                    clazz.getSimpleName());
        }

        if (!clazz.isInstance(this)) {
            throw new VaspException(
                    "Cannot cast %s => %s",
                    getClass().getSimpleName(),
                    clazz.getSimpleName());
        }

        return clazz.cast(this);
    }

}
