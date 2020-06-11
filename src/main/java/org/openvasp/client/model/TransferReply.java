package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
public final class TransferReply extends VaspMessage {

    @JsonProperty("destinationAddr")
    private String destinationAddress;

    {
        getHeader().setMessageType(TypeDescriptor.TRANSFER_REPLY);
    }

}
