package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
public final class TransferDispatch extends VaspMessage {

    @JsonProperty("tx")
    private Tx tx;

    {
        getHeader().setMessageType(TypeDescriptor.TRANSFER_DISPATCH);
    }

    @Override
    public void validate() {
        super.validate();
        validateNotNull(tx, "tx");
        validateNotNull(tx.getDateTime(), "tx.datetime");
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Tx {

        @JsonProperty("txid")
        private String id;

        @JsonProperty("datetime")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX")
        private ZonedDateTime dateTime;

        @JsonProperty("sendingadr")
        private String sendingAddress;

    }

}
