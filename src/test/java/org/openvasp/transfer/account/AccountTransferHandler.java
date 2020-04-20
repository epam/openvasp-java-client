package org.openvasp.transfer.account;

import com.google.common.base.MoreObjects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.openvasp.client.SimpleTransferHandler;
import org.openvasp.client.model.*;
import org.openvasp.client.model.TransferMessage.Transaction;
import org.openvasp.client.session.Session;

import java.time.LocalDateTime;
import java.util.function.BiConsumer;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Slf4j
final class AccountTransferHandler
        extends SimpleTransferHandler
        implements BiConsumer<VaspMessage, Session> {

    private final AccountService accountService;

    public AccountTransferHandler(@NonNull final AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void accept(@NonNull final VaspMessage message, @NonNull final Session session) {
        logMessage(session.vaspCode(), message);
        super.accept(message, session);
    }

    @Override
    protected void onTransferReply(
            @NonNull final TransferReply request,
            @NonNull final TransferDispatch response,
            @NonNull final Session session) {

        val beneficiaryVaan = request.getBeneficiary().getVaan();
        val beneficiaryAccount = accountService.getAccount(beneficiaryVaan);
        response.getTransfer().setDestinationAddress(beneficiaryAccount);
        super.onTransferReply(request, response, session);
    }

    @Override
    protected void onTransferDispatch(
            @NonNull final TransferDispatch request,
            @NonNull final TransferConfirmation response,
            @NonNull final Session session) {

        val originatorVaan = request.getOriginator().getVaan();
        val amount = request.getTransfer().getAmount();
        val originatorAccount = accountService.getAccount(originatorVaan);
        accountService.subtract(originatorAccount, amount);
        val txID = accountService.add(request.getTransfer().getDestinationAddress(), amount);
        var tx = response.getTx();
        if (tx == null) {
            tx = new Transaction();
        }
        tx.setId(txID);
        tx.setDateTime(LocalDateTime.now());
        tx.setSendingAddress(request.getTransfer().getDestinationAddress());
        response.setTx(tx);
        super.onTransferDispatch(request, response, session);
    }

    @Override
    protected void onTransferConfirmation(
            @NonNull final TransferConfirmation request,
            @NonNull final TerminationMessage response,
            @NonNull final Session session) {

        val amount = request.getTransfer().getAmount();
        val tx = request.getTx();
        if (accountService.checkTransaction(tx.getId(), amount)) {
            response.getHeader().setResponseCode(VaspResponseCode.OK.id);
        } else {
            response.getHeader().setResponseCode(VaspResponseCode.TC_ASSETS_NOT_RECEIVED.id);
        }
        super.onTransferConfirmation(request, response, session);
    }

    private void logMessage(@NonNull final VaspCode vaspCode, @NonNull final VaspMessage vaspMessage) {
        log.debug("process {} at {}", vaspMessage.getClass().getSimpleName(), vaspCode);
    }

}
