package org.openvasp.client.api.whisper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ShhNewMessageFilterRequest {

    @JsonProperty("symKeyID")
    private String symKeyId;

    @JsonProperty("privateKeyID")
    private String privateKeyId;

    @JsonProperty
    private List<String> topics;

}
