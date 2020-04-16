package org.openvasp.client.api.whisper;

import org.openvasp.client.common.VaspException;

import java.io.IOException;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public class WhisperIOException extends VaspException {

    public WhisperIOException(IOException ex) {
        super(ex);
    }

}
