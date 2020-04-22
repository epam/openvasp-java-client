package org.openvasp.client.service;

import org.openvasp.client.model.EthAddr;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public interface EnsService {

    EthAddr resolveContractAddress(String contractId);

}
