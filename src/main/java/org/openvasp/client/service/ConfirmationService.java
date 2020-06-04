package org.openvasp.client.service;

import org.openvasp.client.model.VaspMessage;

import java.util.function.Consumer;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public interface ConfirmationService {

    void registerForConfirmation(VaspMessage message);

    void confirmReceipt(VaspMessage message);

    void setConfirmationHandler(Consumer<VaspMessage> handler);

}
