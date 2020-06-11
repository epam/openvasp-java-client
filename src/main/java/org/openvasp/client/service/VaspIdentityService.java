package org.openvasp.client.service;

import org.openvasp.client.model.EthAddr;
import org.openvasp.client.model.VaspMessage;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@FunctionalInterface
public interface VaspIdentityService {

    Optional<EthAddr> resolveSenderVaspId(VaspMessage message);

    default void addVaspIdResolver(Function<VaspMessage, Optional<EthAddr>> resolver) {
    }

}
