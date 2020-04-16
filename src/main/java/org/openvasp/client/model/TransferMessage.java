package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
public abstract class TransferMessage extends VaspMessage {

    @JsonProperty("originator")
    private Originator originator;

    @JsonProperty("beneficiary")
    private Beneficiary beneficiary;

    @JsonProperty("transfer")
    private Transfer transfer;

    @Override
    public void validate() {
        super.validate();
        validateNotNull(originator, "originator");
        validateNotNull(originator.getVaan(), "originator.vaan");
        validateNotNull(beneficiary, "beneficiary");
        validateNotNull(beneficiary.getVaan(), "beneficiary.vaan");
        validateNotNull(transfer, "transfer");
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

        @JsonProperty("destination")
        private String destinationAddress;

    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Transaction {

        @JsonProperty("txid")
        private String id;

        @JsonProperty("datetime")
        private LocalDateTime dateTime;

        @JsonProperty("sendingadr")
        private String sendingAddress;

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

        @JsonCreator
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

    public enum VirtualAssetType {

        @JsonEnumDefaultValue
        ETH,

        BTC;

    }

}
