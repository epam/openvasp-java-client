package org.openvasp.transfer.recording;

import lombok.AllArgsConstructor;
import org.openvasp.client.model.VaspCode;
import org.openvasp.client.model.VaspMessage;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@AllArgsConstructor
final class TransferLogRecord {

    final VaspCode vaspCode;
    final VaspCode peerVaspCode;
    final VaspMessage vaspMessage;

}
