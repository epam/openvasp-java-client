package org.openvasp.client.common;

import lombok.Getter;
import lombok.NonNull;
import org.openvasp.client.model.VaspMessage;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public class VaspValidationException extends VaspException {

    @Getter
    private final VaspMessage source;

    public VaspValidationException(
            @NonNull final VaspMessage source) {

        this.source = source;
    }

    public VaspValidationException(
            @NonNull final VaspMessage source,
            @NonNull final String format,
            Object... args) {

        super(format, args);
        this.source = source;
    }

    public VaspValidationException(
            @NonNull final VaspMessage source,
            @NonNull final Throwable cause) {

        super(cause);
        this.source = source;
    }

    public VaspValidationException(
            @NonNull final VaspMessage source,
            @NonNull final String message,
            @NonNull final Throwable cause) {

        super(message, cause);
        this.source = source;
    }

}
