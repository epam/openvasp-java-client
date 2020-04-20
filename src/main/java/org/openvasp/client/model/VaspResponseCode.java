package org.openvasp.client.model;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public enum VaspResponseCode {

    // Common response codes
    OK("1"),
    INVALID_REQUEST("2"),
    SERVICE_NOT_AVAILABLE("5"),

    // SessionReply
    SR_VASP_AUTH_ERROR("3"),
    SR_VASP_DECLINED("4"),

    // TransferReply
    TR_NO_SUCH_BENEFICIARY("3"),
    TR_VA_NOT_SUPPORTED("4"),
    TR_TRANSFER_AUTH_ERROR("5"),
    TR_SERVICE_NOT_AVAILABLE("6"),

    // TransferConfirmation
    TC_ASSETS_NOT_RECEIVED("3"),
    TC_WRONG_AMOUNT("4"),
    TC_WRONG_ASSET("5"),
    TC_TX_DATA_MISMATCH("6");

    public final String id;

    VaspResponseCode(String id) {
        this.id = id;
    }

}
