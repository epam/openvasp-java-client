package org.openvasp.client.api.whisper;

import org.openvasp.client.api.whisper.model.ShhMessage;
import org.openvasp.client.api.whisper.model.ShhNewMessageFilterRequest;
import org.openvasp.client.api.whisper.model.ShhPostRequest;

import java.util.List;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public interface WhisperApi extends WhisperSymKeyApi{

    String version();

    // Key pairs API ----------------------------------------------------------

    String newKeyPair();

    String addPrivateKey(String key);

    Boolean deleteKeyPair(String keyPairId);

    Boolean hasKeyPair(String keyPairId);

    String getPrivateKey(String keyPairId);

    String getPublicKey(String keyPairId);

    // Sym keys API -----------------------------------------------------------

    @Override
    String newSymKey();

    @Override
    String generateSymKeyFromPassword(String password);

    @Override
    String addSymKey(String key);

    @Override
    Boolean deleteSymKey(String symKeyId);

    @Override
    Boolean hasSymKey(String symKeyId);

    @Override
    String getSymKey(String symKeyId);

    // Send/receive messages --------------------------------------------------

    String newMessageFilter(ShhNewMessageFilterRequest filterRequest);

    Boolean deleteMessageFilter(String filterId);

    List<ShhMessage> getFilterMessages(String filterId);

    String post(ShhPostRequest postRequest);

}
