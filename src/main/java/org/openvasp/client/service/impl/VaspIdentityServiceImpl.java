package org.openvasp.client.service.impl;

import lombok.NonNull;
import lombok.val;
import org.openvasp.client.model.EthAddr;
import org.openvasp.client.model.VaspMessage;
import org.openvasp.client.service.VaspIdentityService;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Singleton
public final class VaspIdentityServiceImpl implements VaspIdentityService {

    private final List<Function<VaspMessage, Optional<EthAddr>>> resolvers = new ArrayList<>();

    @Override
    public Optional<EthAddr> resolveSenderVaspId(@NonNull final VaspMessage message) {
        for (val f : resolvers) {
            val result = f.apply(message);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    @Override
    public void addVaspIdResolver(@NonNull final Function<VaspMessage, Optional<EthAddr>> resolver) {
        resolvers.add(resolver);
    }

}
