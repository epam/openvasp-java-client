package org.openvasp.client.api.whisper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ShhPostRequest {

    @JsonProperty("symKeyID")
    private String symKeyId;

    @JsonProperty
    private String pubKey;

    @JsonProperty
    private BigInteger ttl;

    @JsonProperty
    private String topic;

    @JsonProperty
    private String payload;

    @JsonProperty
    private BigInteger powTime;

    @JsonProperty
    private BigDecimal powTarget;

}
