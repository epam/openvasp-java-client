package org.openvasp.client.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
public final class TransferConfirmation extends VaspMessage {

    {
        getHeader().setMessageType(TypeDescriptor.TRANSFER_CONFIRMATION);
    }

}
