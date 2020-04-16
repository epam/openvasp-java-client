package org.openvasp.client.model;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public final class TransferReply extends TransferMessage {

    {
        getHeader().setMessageType(TypeDescriptor.TRANSFER_REPLY);
    }

}
