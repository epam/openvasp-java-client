package org.openvasp.client.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Getter
@Setter
public abstract class SessionMessage extends VaspMessage {

    @JsonProperty("vasp")
    private VaspInfo vaspInfo;

    @JsonIgnore
    public final VaspCode getSenderVaspCode() {
        checkState(vaspInfo != null);
        checkState(vaspInfo.getVaspId() != null);
        return vaspInfo.getVaspCode();
    }

    @Override
    public void validate() {
        super.validate();
        validateNotNull(vaspInfo, "vasp");
        validateNotNull(vaspInfo.getName(), "vasp.name");
        validateNotNull(vaspInfo.getVaspId(), "vasp.id");
        validateNotNull(vaspInfo.getPk(), "vasp.pk");
        validateNotNull(vaspInfo.getAddress(), "vasp.address");
        vaspInfo.validate(this);
    }

}
