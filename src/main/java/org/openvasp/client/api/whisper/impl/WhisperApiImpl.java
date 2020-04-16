package org.openvasp.client.api.whisper.impl;

import lombok.SneakyThrows;
import lombok.val;
import org.openvasp.client.common.annotation.WhisperNode;
import org.openvasp.client.api.whisper.WhisperApi;
import org.openvasp.client.api.whisper.WhisperApiError;
import org.openvasp.client.api.whisper.WhisperAsyncApi;
import org.openvasp.client.api.whisper.WhisperIOException;
import org.openvasp.client.api.whisper.model.ShhMessage;
import org.openvasp.client.api.whisper.model.ShhNewMessageFilterRequest;
import org.openvasp.client.api.whisper.model.ShhPostRequest;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Singleton
public final class WhisperApiImpl implements WhisperApi, WhisperAsyncApi {

    private static final String SHH_VERSION = "shh_version";
    private static final String SHH_NEW_KEY_PAIR = "shh_newKeyPair";
    private static final String SHH_ADD_PRIVATE_KEY = "shh_addPrivateKey";
    private static final String SHH_DELETE_KEY_PAIR = "shh_deleteKeyPair";
    private static final String SHH_HAS_KEY_PAIR = "shh_hasKeyPair";
    private static final String SHH_GET_PRIVATE_KEY = "shh_getPrivateKey";
    private static final String SHH_GET_PUBLIC_KEY = "shh_getPublicKey";
    private static final String SHH_NEW_SYM_KEY = "shh_newSymKey";
    private static final String SHH_GENERATE_SYM_KEY_FROM_PASSWORD = "shh_generateSymKeyFromPassword";
    private static final String SHH_ADD_SYM_KEY = "shh_addSymKey";
    private static final String SHH_DELETE_SYM_KEY = "shh_deleteSymKey";
    private static final String SHH_HAS_SYM_KEY = "shh_hasSymKey";
    private static final String SHH_GET_SYM_KEY = "shh_getSymKey";
    private static final String SHH_NEW_MESSAGE_FILTER = "shh_newMessageFilter";
    private static final String SHH_DELETE_MESSAGE_FILTER = "shh_deleteMessageFilter";
    private static final String SHH_GET_FILTER_MESSAGES = "shh_getFilterMessages";
    private static final String SHH_POST = "shh_post";

    private final Web3jService web3jService;

    @Inject
    public WhisperApiImpl(@WhisperNode final Web3jService web3jService) {
        this.web3jService = web3jService;
    }

    //-------------------------------------------------------------------------
    // Version

    @Override
    public String version() {
        val request = new Request<>(
                SHH_VERSION,
                Collections.<String>emptyList(),
                web3jService,
                StringResponse.class);
        return invoke(request);
    }

    @Override
    public CompletableFuture<String> versionAsync() {
        val request = new Request<>(
                SHH_VERSION,
                Collections.<String>emptyList(),
                web3jService,
                StringResponse.class);
        return invokeAsync(request);
    }

    //-------------------------------------------------------------------------
    // Key pairs

    @Override
    public String newKeyPair() {
        val request = new Request<>(
                SHH_NEW_KEY_PAIR,
                Collections.<String>emptyList(),
                web3jService,
                StringResponse.class);
        return invoke(request);
    }

    @Override
    public CompletableFuture<String> newKeyPairAsync() {
        val request = new Request<>(
                SHH_NEW_KEY_PAIR,
                Collections.<String>emptyList(),
                web3jService,
                StringResponse.class);
        return invokeAsync(request);
    }

    @Override
    public String addPrivateKey(final String key) {
        val request = new Request<>(
                SHH_ADD_PRIVATE_KEY,
                Collections.<String>singletonList(key),
                web3jService,
                StringResponse.class);
        return invoke(request);
    }

    @Override
    public CompletableFuture<String> addPrivateKeyAsync(final String key) {
        val request = new Request<>(
                SHH_ADD_PRIVATE_KEY,
                Collections.<String>singletonList(key),
                web3jService,
                StringResponse.class);
        return invokeAsync(request);
    }

    @Override
    public Boolean deleteKeyPair(final String keyPairId) {
        val request = new Request<>(
                SHH_DELETE_KEY_PAIR,
                Collections.singletonList(keyPairId),
                web3jService,
                BooleanResponse.class);
        return invoke(request);
    }

    @Override
    public CompletableFuture<Boolean> deleteKeyPairAsync(final String keyPairId) {
        val request = new Request<>(
                SHH_DELETE_KEY_PAIR,
                Collections.singletonList(keyPairId),
                web3jService,
                BooleanResponse.class);
        return invokeAsync(request);
    }

    @Override
    public Boolean hasKeyPair(final String keyPairId) {
        val request = new Request<>(
                SHH_HAS_KEY_PAIR,
                Collections.singletonList(keyPairId),
                web3jService,
                BooleanResponse.class);
        return invoke(request);
    }

    @Override
    public CompletableFuture<Boolean> hasKeyPairAsync(final String keyPairId) {
        val request = new Request<>(
                SHH_HAS_KEY_PAIR,
                Collections.singletonList(keyPairId),
                web3jService,
                BooleanResponse.class);
        return invokeAsync(request);
    }

    @Override
    public String getPrivateKey(final String keyPairId) {
        val request = new Request<>(
                SHH_GET_PRIVATE_KEY,
                Collections.<String>singletonList(keyPairId),
                web3jService,
                StringResponse.class);
        return invoke(request);
    }

    @Override
    public CompletableFuture<String> getPrivateKeyAsync(final String keyPairId) {
        val request = new Request<>(
                SHH_GET_PRIVATE_KEY,
                Collections.<String>singletonList(keyPairId),
                web3jService,
                StringResponse.class);
        return invokeAsync(request);
    }

    @Override
    public String getPublicKey(final String keyPairId) {
        val request = new Request<>(
                SHH_GET_PUBLIC_KEY,
                Collections.<String>singletonList(keyPairId),
                web3jService,
                StringResponse.class);
        return invoke(request);
    }

    @Override
    public CompletableFuture<String> getPublicKeyAsync(final String keyPairId) {
        val request = new Request<>(
                SHH_GET_PUBLIC_KEY,
                Collections.<String>singletonList(keyPairId),
                web3jService,
                StringResponse.class);
        return invokeAsync(request);
    }

    //-------------------------------------------------------------------------
    // Sym keys

    @Override
    public String newSymKey() {
        val request = new Request<>(
                SHH_NEW_SYM_KEY,
                Collections.<String>emptyList(),
                web3jService,
                StringResponse.class);
        return invoke(request);
    }

    @Override
    public CompletableFuture<String> newSymKeyAsync() {
        val request = new Request<>(
                SHH_NEW_SYM_KEY,
                Collections.<String>emptyList(),
                web3jService,
                StringResponse.class);
        return invokeAsync(request);
    }

    @Override
    public String generateSymKeyFromPassword(final String password) {
        val request = new Request<>(
                SHH_GENERATE_SYM_KEY_FROM_PASSWORD,
                Collections.singletonList(password),
                web3jService,
                StringResponse.class);
        return invoke(request);
    }

    @Override
    public CompletableFuture<String> generateSymKeyFromPasswordAsync(final String password) {
        val request = new Request<>(
                SHH_GENERATE_SYM_KEY_FROM_PASSWORD,
                Collections.singletonList(password),
                web3jService,
                StringResponse.class);
        return invokeAsync(request);
    }

    @Override
    public String addSymKey(final String key) {
        val request = new Request<>(
                SHH_ADD_SYM_KEY,
                Collections.singletonList(key),
                web3jService,
                StringResponse.class);
        return invoke(request);
    }

    @Override
    public CompletableFuture<String> addSymKeyAsync(final String key) {
        val request = new Request<>(
                SHH_ADD_SYM_KEY,
                Collections.singletonList(key),
                web3jService,
                StringResponse.class);
        return invokeAsync(request);
    }

    @Override
    public Boolean deleteSymKey(final String symKeyId) {
        val request = new Request<>(
                SHH_DELETE_SYM_KEY,
                Collections.singletonList(symKeyId),
                web3jService,
                BooleanResponse.class);
        return invoke(request);
    }

    @Override
    public CompletableFuture<Boolean> deleteSymKeyAsync(final String symKeyId) {
        val request = new Request<>(
                SHH_DELETE_SYM_KEY,
                Collections.singletonList(symKeyId),
                web3jService,
                BooleanResponse.class);
        return invokeAsync(request);
    }

    @Override
    public Boolean hasSymKey(final String symKeyId) {
        val request = new Request<>(
                SHH_HAS_SYM_KEY,
                Collections.singletonList(symKeyId),
                web3jService,
                BooleanResponse.class);
        return invoke(request);
    }

    @Override
    public CompletableFuture<Boolean> hasSymKeyAsync(final String symKeyId) {
        val request = new Request<>(
                SHH_HAS_SYM_KEY,
                Collections.singletonList(symKeyId),
                web3jService,
                BooleanResponse.class);
        return invokeAsync(request);
    }

    @Override
    public String getSymKey(final String symKeyId) {
        val request = new Request<>(
                SHH_GET_SYM_KEY,
                Collections.singletonList(symKeyId),
                web3jService,
                StringResponse.class);
        return invoke(request);
    }

    @Override
    public CompletableFuture<String> getSymKeyAsync(final String symKeyId) {
        val request = new Request<>(
                SHH_GET_SYM_KEY,
                Collections.singletonList(symKeyId),
                web3jService,
                StringResponse.class);
        return invokeAsync(request);
    }

    //-------------------------------------------------------------------------
    // Send/receive messages

    @Override
    public String newMessageFilter(final ShhNewMessageFilterRequest filterRequest) {
        val request = new Request<>(
                SHH_NEW_MESSAGE_FILTER,
                Collections.singletonList(filterRequest),
                web3jService,
                StringResponse.class);
        return invoke(request);
    }

    @Override
    public CompletableFuture<String> newMessageFilterAsync(final ShhNewMessageFilterRequest filterRequest) {
        val request = new Request<>(
                SHH_NEW_MESSAGE_FILTER,
                Collections.singletonList(filterRequest),
                web3jService,
                StringResponse.class);
        return invokeAsync(request);
    }

    @Override
    public Boolean deleteMessageFilter(final String filterId) {
        val request = new Request<>(
                SHH_DELETE_MESSAGE_FILTER,
                Collections.singletonList(filterId),
                web3jService,
                BooleanResponse.class);
        return invoke(request);
    }

    @Override
    public CompletableFuture<Boolean> deleteMessageFilterAsync(final String filterId) {
        val request = new Request<>(
                SHH_DELETE_MESSAGE_FILTER,
                Collections.singletonList(filterId),
                web3jService,
                BooleanResponse.class);
        return invokeAsync(request);
    }

    @Override
    public List<ShhMessage> getFilterMessages(final String filterId) {
        val request = new Request<>(
                SHH_GET_FILTER_MESSAGES,
                Collections.singletonList(filterId),
                web3jService,
                MessageListResponse.class);
        return invoke(request);
    }

    @Override
    public CompletableFuture<List<ShhMessage>> getFilterMessagesAsync(final String filterId) {
        val request = new Request<>(
                SHH_GET_FILTER_MESSAGES,
                Collections.singletonList(filterId),
                web3jService,
                MessageListResponse.class);
        return invokeAsync(request);
    }

    @Override
    public String post(final ShhPostRequest postRequest) {
        val request = new Request<>(
                SHH_POST,
                Collections.singletonList(postRequest),
                web3jService,
                StringResponse.class);
        return invoke(request);
    }

    @Override
    public CompletableFuture<String> postAsync(final ShhPostRequest postRequest) {
        val request = new Request<>(
                SHH_POST,
                Collections.singletonList(postRequest),
                web3jService,
                StringResponse.class);
        return invokeAsync(request);
    }

    //-------------------------------------------------------------------------

    private static <T, R extends Response<T>> T invoke(final Request<?, R> request) {
        try {
            val result = request.send();
            if (result.hasError()) {
                throw new WhisperApiError(result.getError(), result.getRawResponse());
            }
            return result.getResult();
        } catch (IOException ex) {
            throw new WhisperIOException(ex);
        }
    }

    @SneakyThrows
    private static <T, R extends Response<T>> CompletableFuture<T> invokeAsync(final Request<?, R> request) {
        return request.sendAsync().thenCompose(response -> {
            val result = new CompletableFuture<T>();
            if (response.hasError()) {
                result.complete(response.getResult());
            } else {
                result.completeExceptionally(new WhisperApiError(response.getError(), response.getRawResponse()));
            }
            return result;
        });
    }

    //-------------------------------------------------------------------------
    // Models

    public static class StringResponse extends Response<String> {
    }

    public static class BooleanResponse extends Response<Boolean> {
    }

    public static class MessageListResponse extends Response<List<ShhMessage>> {
    }

}
