package org.openvasp.client.model;

import lombok.*;

import static org.openvasp.client.model.TransferMessage.Transaction;
import static org.openvasp.client.model.TransferMessage.Transfer;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class TransferInfo {

    private Originator originator;
    private Beneficiary beneficiary;
    private Transfer transfer;

    @Setter
    private Transaction tx;

}
