package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.val;

import java.util.NoSuchElementException;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public enum EncryptionType {

    ASSYMETRIC(1),
    SYMMETRIC(2);

    public final int id;

    EncryptionType(int id) {
        this.id = id;
    }

    @JsonValue
    public int getId() {
        return id;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static EncryptionType fromId(int id) {
        for (val item : EncryptionType.values()) {
            if (item.id == id) {
                return item;
            }
        }

        throw new NoSuchElementException(String.format(
                "%s with id = %d does not exist",
                EncryptionType.class.getName(),
                id));
    }

}
