package org.openvasp.client.model;

import lombok.*;
import org.openvasp.client.model.TransferDispatch.Tx;
import org.openvasp.client.model.TransferRequest.Transfer;

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
    private Tx tx;
    private String destinationAddress;

}
