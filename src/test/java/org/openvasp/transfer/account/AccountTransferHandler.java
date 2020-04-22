package org.openvasp.transfer.account;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.openvasp.client.SimpleTransferHandler;
import org.openvasp.client.model.*;
import org.openvasp.client.model.TransferMessage.Transaction;
import org.openvasp.client.session.Session;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Slf4j
final class AccountTransferHandler implements SimpleTransferHandler {

    private final AccountService accountService;

    public AccountTransferHandler(@NonNull final AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void accept(@NonNull final VaspMessage message, @NonNull final Session session) {
        logMessage(session.vaspCode(), message);
        SimpleTransferHandler.super.accept(message, session);
    }

    @Override
    public void onTransferReply(
            @NonNull final TransferReply request,
            @NonNull final TransferDispatch response,
            @NonNull final Session session) {

        final Vaan beneficiaryVaan = request.getBeneficiary().getVaan();
        final String beneficiaryAccount = accountService.getAccount(beneficiaryVaan);
        response.getTransfer().setDestinationAddress(beneficiaryAccount);
        response.setTx(new Transaction());
        response.getTx().setDateTime(LocalDateTime.now());
        SimpleTransferHandler.super.onTransferReply(request, response, session);
    }

    @Override
    public void onTransferDispatch(
            @NonNull final TransferDispatch request,
            @NonNull final TransferConfirmation response,
            @NonNull final Session session) {

        final Vaan originatorVaan = request.getOriginator().getVaan();
        final BigDecimal amount = request.getTransfer().getAmount();
        final String originatorAccount = accountService.getAccount(originatorVaan);
        accountService.subtract(originatorAccount, amount);
        final String txID = accountService.add(request.getTransfer().getDestinationAddress(), amount);
        Transaction tx = response.getTx();
        if (tx == null) {
            tx = new Transaction();
        }
        tx.setId(txID);
        tx.setDateTime(LocalDateTime.now());
        tx.setSendingAddress(request.getTransfer().getDestinationAddress());
        response.setTx(tx);
        SimpleTransferHandler.super.onTransferDispatch(request, response, session);
    }

    @Override
    public void onTransferConfirmation(
            @NonNull final TransferConfirmation request,
            @NonNull final TerminationMessage response,
            @NonNull final Session session) {

        final BigDecimal amount = request.getTransfer().getAmount();
        final Transaction tx = request.getTx();
        if (accountService.checkTransaction(tx.getId(), amount)) {
            response.getHeader().setResponseCode(VaspResponseCode.OK.id);
        } else {
            response.getHeader().setResponseCode(VaspResponseCode.TC_ASSETS_NOT_RECEIVED.id);
        }
        SimpleTransferHandler.super.onTransferConfirmation(request, response, session);
    }

    private void logMessage(@NonNull final VaspCode vaspCode, @NonNull final VaspMessage vaspMessage) {
        log.debug("process {} at {}", vaspMessage.getClass().getSimpleName(), vaspCode);
    }

}
