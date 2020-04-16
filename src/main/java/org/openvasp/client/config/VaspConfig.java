package org.openvasp.client.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openvasp.client.model.VaspCode;
import org.openvasp.client.model.VaspInfo;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class VaspConfig {

    @JsonProperty
    private String httpServiceUrl;

    @JsonProperty
    private String whisperNodeUrl;

    @JsonProperty
    private String contractNodeUrl;

    @JsonProperty
    private VaspCode vaspCode;

    @JsonProperty
    private String handshakePrivateKey;

    @JsonProperty
    private String signingPrivateKey;

    @JsonProperty
    private VaspInfo vaspInfo;

}
