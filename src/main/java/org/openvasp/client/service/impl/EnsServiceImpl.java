package org.openvasp.client.service.impl;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openvasp.client.common.VaspException;
import org.openvasp.client.common.annotation.ContractNode;
import org.openvasp.client.model.EthAddr;
import org.openvasp.client.service.EnsService;
import org.web3j.ens.EnsResolutionException;
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

    private static final int MAX_ATTEMPTS = 4;
    private static final long ATTEMPT_TIMEOUT_MS = 500;

    private final EnsResolver ensResolver;
    private final Map<String, EthAddr> cache = new ConcurrentHashMap<>();

    @Inject
    public EnsServiceImpl(@ContractNode final Web3j web3j) {
        this.ensResolver = new EnsResolver(web3j);
    }

    @Override
    @SneakyThrows
    public EthAddr resolveContractAddress(@NonNull final String contractId) {
        EthAddr result = cache.get(contractId);
        EnsResolutionException ensResolutionException = null;
        for (int i = 1; result == null && i <= MAX_ATTEMPTS; i++) {
            try {
                result = new EthAddr(ensResolver.resolve(contractId));
            } catch (EnsResolutionException ex) {
                log.warn("Attempt {} to resolve ENS ID {} failed", i, contractId);
                ensResolutionException = ex;
                Thread.sleep(ATTEMPT_TIMEOUT_MS);
            }
        }

        if (result != null) {
            cache.putIfAbsent(contractId, result);
            return result;
        } else {
            throw new VaspException(ensResolutionException, "ENS resolution for the contract ID = %s failed", contractId);
        }
    }

}
