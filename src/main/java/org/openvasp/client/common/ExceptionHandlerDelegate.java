package org.openvasp.client.common;

import lombok.NonNull;
import lombok.Setter;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public final class ExceptionHandlerDelegate implements ExceptionHandler {

    @Setter
    private ExceptionHandler delegate;

    @Override
    public void processException(@NonNull final Exception ex) {
        if (delegate != null) {
            delegate.processException(ex);
        }
    }

}
