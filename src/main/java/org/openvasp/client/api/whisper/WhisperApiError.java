package org.openvasp.client.api.whisper;

import lombok.Getter;
import org.web3j.protocol.core.Response;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public class WhisperApiError extends RuntimeException {

    @Getter
    private final Response.Error error;

    @Getter
    private final String rawResponse;

    public WhisperApiError(final Response.Error error, final String rawResponse) {
        super(error.getMessage());
        this.error = error;
        this.rawResponse = rawResponse;
    }

}
