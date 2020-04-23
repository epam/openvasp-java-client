package org.openvasp.host.cli;

import lombok.NonNull;
import lombok.val;
import org.bouncycastle.util.encoders.Base64;
import org.openvasp.client.config.VaspConfig;
import org.openvasp.client.config.VaspModule;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public class CliHostModule extends VaspModule {

    public CliHostModule(@NonNull final VaspConfig vaspConfig, @NonNull final String infuraSecret) {
        super(vaspConfig);
        val infuraSecretBytes = (":" + infuraSecret).getBytes();
        val auth = Base64.toBase64String(infuraSecretBytes);
        contractHttpService.addHeader("Authorization", "Basic " + auth);
    }

}
