package org.openvasp.client.model;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public enum VaspResponseCode {

    OK("1"),
    INVALID_REQUEST("2");

    public final String id;

    VaspResponseCode(String id) {
        this.id = id;
    }

}
