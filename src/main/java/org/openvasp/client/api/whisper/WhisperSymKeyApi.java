package org.openvasp.client.api.whisper;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public interface WhisperSymKeyApi {

    String newSymKey();

    String generateSymKeyFromPassword(String password);

    String addSymKey(String key);

    Boolean deleteSymKey(String symKeyId);

    Boolean hasSymKey(String symKeyId);

    String getSymKey(String symKeyId);

}
