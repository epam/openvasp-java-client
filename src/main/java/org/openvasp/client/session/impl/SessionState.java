package org.openvasp.client.session.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.openvasp.client.model.Topic;
import org.openvasp.client.model.TransferInfo;
import org.openvasp.client.model.VaspInfo;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public final class SessionState {

    enum Type {
        ORIGINATOR,
        BENEFICIARY
    }

    @JsonProperty
    private String id;

    @JsonProperty
    private Type type;

    @JsonProperty
    private Topic incomingTopic;

    @JsonProperty
    private Topic outgoingTopic;

    @JsonProperty
    private String sharedSecret;

    @JsonProperty
    private String sessionPublicKey;

    @JsonProperty
    private TransferInfo transferInfo;

    @JsonProperty
    VaspInfo peerVaspInfo;

}
