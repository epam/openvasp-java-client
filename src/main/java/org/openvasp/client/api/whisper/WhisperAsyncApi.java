package org.openvasp.client.api.whisper;

import org.openvasp.client.api.whisper.model.ShhMessage;
import org.openvasp.client.api.whisper.model.ShhNewMessageFilterRequest;
import org.openvasp.client.api.whisper.model.ShhPostRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public interface WhisperAsyncApi {

    CompletableFuture<String> versionAsync();

    // Key pairs API ----------------------------------------------------------

    CompletableFuture<String> newKeyPairAsync();

    CompletableFuture<String> addPrivateKeyAsync(String key);

    CompletableFuture<Boolean> deleteKeyPairAsync(String keyPairId);

    CompletableFuture<Boolean> hasKeyPairAsync(String keyPairId);

    CompletableFuture<String> getPrivateKeyAsync(String keyPairId);

    CompletableFuture<String> getPublicKeyAsync(String keyPairId);

    // Sym keys API -----------------------------------------------------------

    CompletableFuture<String> newSymKeyAsync();

    CompletableFuture<String> generateSymKeyFromPasswordAsync(String password);

    CompletableFuture<String> addSymKeyAsync(String key);

    CompletableFuture<Boolean> deleteSymKeyAsync(String symKeyId);

    CompletableFuture<Boolean> hasSymKeyAsync(String symKeyId);

    CompletableFuture<String> getSymKeyAsync(String symKeyId);

    // Send/receive messages --------------------------------------------------

    CompletableFuture<String> newMessageFilterAsync(ShhNewMessageFilterRequest filterRequest);

    CompletableFuture<Boolean> deleteMessageFilterAsync(String filterId);

    CompletableFuture<List<ShhMessage>> getFilterMessagesAsync(String filterId);

    CompletableFuture<String> postAsync(ShhPostRequest postRequest);

}
