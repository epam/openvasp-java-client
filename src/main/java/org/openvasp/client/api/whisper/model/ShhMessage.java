package org.openvasp.client.api.whisper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Data
@NoArgsConstructor
public final class ShhMessage {

    @JsonProperty
    private String sig;

    @JsonProperty
    private BigInteger ttl;

    @JsonProperty
    private Long timestamp;

    @JsonProperty
    private String topic;

    @JsonProperty
    private String payload;

    @JsonProperty
    private String padding;

    @JsonProperty
    private BigDecimal pow;

    @JsonProperty
    private String hash;

    @JsonProperty
    private String recipientPublicKey;

}
