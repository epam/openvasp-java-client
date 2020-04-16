package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Data
public final class InfuraConfig {

    @JsonProperty
    private String endpoint;

    @JsonProperty
    private String secret;

}
