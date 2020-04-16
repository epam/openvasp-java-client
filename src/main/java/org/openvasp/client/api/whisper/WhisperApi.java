package org.openvasp.client.api.whisper;

import org.openvasp.client.api.whisper.model.ShhMessage;
import org.openvasp.client.api.whisper.model.ShhNewMessageFilterRequest;
import org.openvasp.client.api.whisper.model.ShhPostRequest;

import java.util.List;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public interface WhisperApi {

    String version();

    // Key pairs API ----------------------------------------------------------

    String newKeyPair();

    String addPrivateKey(String key);

    Boolean deleteKeyPair(String keyPairId);

    Boolean hasKeyPair(String keyPairId);

    String getPrivateKey(String keyPairId);

    String getPublicKey(String keyPairId);

    // Sym keys API -----------------------------------------------------------

    String newSymKey();

    String generateSymKeyFromPassword(String password);

    String addSymKey(String key);

    Boolean deleteSymKey(String symKeyId);

    Boolean hasSymKey(String symKeyId);

    String getSymKey(String symKeyId);

    // Send/receive messages --------------------------------------------------

    String newMessageFilter(ShhNewMessageFilterRequest filterRequest);

    Boolean deleteMessageFilter(String filterId);

    List<ShhMessage> getFilterMessages(String filterId);

    String post(ShhPostRequest postRequest);

}
