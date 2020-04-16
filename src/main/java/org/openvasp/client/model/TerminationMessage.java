package org.openvasp.client.model;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public final class TerminationMessage extends VaspMessage {

    {
        getHeader().setMessageType(TypeDescriptor.TERMINATION);
    }

}
