package org.openvasp.client.service;

import org.openvasp.client.model.VaspMessage;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public interface SignService {

    String makeSignedPayload(VaspMessage message, String privateKey);

    VaspMessage extractSignedMessage(String whisperPayload);

}
