package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class VaspContractInfo {

    private String name;
    private VaspCode vaspCode;
    private List<Long> channels;
    private String handshakeKey;
    private String signingKey;
    private String ownerAddress;
    private PostalAddress address;
    private String email;
    private String website;

}
