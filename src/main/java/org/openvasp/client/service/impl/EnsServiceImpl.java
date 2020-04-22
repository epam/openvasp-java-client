package org.openvasp.client.service.impl;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.openvasp.client.common.annotation.ContractNode;
import org.openvasp.client.model.EthAddr;
import org.openvasp.client.service.EnsService;
import org.web3j.ens.EnsResolver;
import org.web3j.protocol.Web3j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Singleton
@Slf4j
public final class EnsServiceImpl implements EnsService {

    private final EnsResolver ensResolver;
    private final Map<String, EthAddr> cache = new ConcurrentHashMap<>();

    @Inject
    public EnsServiceImpl(@ContractNode final Web3j web3j) {
        this.ensResolver = new EnsResolver(web3j);
    }

    @Override
    public EthAddr resolveContractAddress(@NonNull final String contractId) {
        EthAddr result = cache.get(contractId);
        if (result == null) {
            result = new EthAddr(ensResolver.resolve(contractId));
            cache.putIfAbsent(contractId, result);
        }
        return result;
    }

}
