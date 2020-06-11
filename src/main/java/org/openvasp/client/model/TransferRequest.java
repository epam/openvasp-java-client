package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
public final class TransferRequest extends VaspMessage {

    @JsonProperty("originator")
    private Originator originator;

    @JsonProperty("beneficiary")
    private Beneficiary beneficiary;

    @JsonProperty("transfer")
    private Transfer transfer;

    {
        getHeader().setMessageType(TypeDescriptor.TRANSFER_REQUEST);
    }

    @Override
    public void validate() {
        super.validate();
        validateNotNull(originator, "originator");
        validateNotNull(originator.getVaan(), "originator.vaan");
        originator.validate(this);
        validateNotNull(beneficiary, "beneficiary");
        validateNotNull(beneficiary.getVaan(), "beneficiary.vaan");
        beneficiary.validate(this);
        validateNotNull(transfer, "transfer");
        validateNotNull(transfer.getAssetType(), "transfer.va");
        validateNotNull(transfer.getTransferType(), "transfer.ttype");
        validateNotNull(transfer.getAmount(), "transfer.amount");
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Transfer {

        @JsonProperty("va")
        private VirtualAssetType assetType;

        @JsonProperty("ttype")
        private TransferType transferType;

        @JsonProperty("amount")
        private BigDecimal amount;

    }

    public enum VirtualAssetType {

        @JsonEnumDefaultValue
        ETH,

        BTC;

    }

    public enum TransferType {

        BLOCKCHAIN_TRANSFER(1);

        public final int id;

        TransferType(int id) {
            this.id = id;
        }

        @JsonValue
        public int getId() {
            return id;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static TransferType fromId(int id) {
            for (val item : TransferType.values()) {
                if (item.id == id) {
                    return item;
                }
            }

            throw new NoSuchElementException(String.format(
                    "%s with id = %d does not exist",
                    TransferType.class.getName(),
                    id));
        }

    }

}
