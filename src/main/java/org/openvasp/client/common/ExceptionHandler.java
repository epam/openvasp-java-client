package org.openvasp.client.common;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@FunctionalInterface
public interface ExceptionHandler {

    void processException(Exception ex);

}
