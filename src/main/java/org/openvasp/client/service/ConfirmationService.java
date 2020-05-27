package org.openvasp.client.service;

import org.openvasp.client.model.VaspMessage;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public interface ConfirmationService extends AutoCloseable {

    void registerForConfirmation(VaspMessage message);

    void confirmReceipt(VaspMessage message);

}
