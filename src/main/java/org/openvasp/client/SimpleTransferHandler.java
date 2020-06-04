package org.openvasp.client;

import lombok.NonNull;
import lombok.val;
import org.openvasp.client.model.*;
import org.openvasp.client.session.Session;

import java.util.function.BiConsumer;

import static org.openvasp.client.model.VaspResponseCode.OK;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public interface SimpleTransferHandler extends BiConsumer<VaspMessage, Session> {

    @Override
    default void accept(@NonNull final VaspMessage message, @NonNull final Session session) {
        if (message instanceof SessionRequest) {
            val response = new SessionReply();
            response.getHeader().setResponseCode(OK.id);
            onSessionRequest((SessionRequest) message, response, session);
        }

        if (message instanceof SessionReply) {
            val response = new TransferRequest();
            response.setOriginator(session.transferInfo().getOriginator());
            response.setBeneficiary(session.transferInfo().getBeneficiary());
            response.setTransfer(session.transferInfo().getTransfer());
            onSessionReply((SessionReply) message, response, session);
        }

        if (message instanceof TransferRequest) {
            val request = (TransferRequest) message;
            val response = new TransferReply();
            response.setOriginator(request.getOriginator());
            response.setBeneficiary(request.getBeneficiary());
            response.setTransfer(request.getTransfer());
            onTransferRequest(request, response, session);
        }

        if (message instanceof TransferReply) {
            val response = new TransferDispatch();
            response.setOriginator(session.transferInfo().getOriginator());
            response.setBeneficiary(session.transferInfo().getBeneficiary());
            response.setTransfer(session.transferInfo().getTransfer());
            response.setTx(session.transferInfo().getTx());
            onTransferReply((TransferReply) message, response, session);
        }

        if (message instanceof TransferDispatch) {
            val response = new TransferConfirmation();
            response.setOriginator(session.transferInfo().getOriginator());
            response.setBeneficiary(session.transferInfo().getBeneficiary());
            response.setTransfer(session.transferInfo().getTransfer());
            response.setTx(session.transferInfo().getTx());
            onTransferDispatch((TransferDispatch) message, response, session);
        }

        if (message instanceof TransferConfirmation) {
            val response = new TerminationMessage();
            onTransferConfirmation((TransferConfirmation) message, response, session);
            session.remove();
        }

        if (message instanceof TerminationMessage) {
            onTerminationMessage((TerminationMessage) message, session);
            session.remove();
        }
    }

    default void onSessionRequest(
            @NonNull SessionRequest request,
            @NonNull SessionReply response,
            @NonNull Session session) {

        session.sendMessage(response);
    }

    default void onSessionReply(
            @NonNull SessionReply request,
            @NonNull TransferRequest response,
            @NonNull Session session) {

        session.sendMessage(response);
    }

    default void onTransferRequest(
            @NonNull TransferRequest request,
            @NonNull TransferReply response,
            @NonNull Session session) {

        session.sendMessage(response);
    }

    default void onTransferReply(
            @NonNull TransferReply request,
            @NonNull TransferDispatch response,
            @NonNull Session session) {

        session.sendMessage(response);
    }

    default void onTransferDispatch(
            @NonNull TransferDispatch request,
            @NonNull TransferConfirmation response,
            @NonNull Session session) {

        session.sendMessage(response);
    }

    default void onTransferConfirmation(
            @NonNull TransferConfirmation request,
            @NonNull TerminationMessage response,
            @NonNull Session session) {

        session.sendMessage(response);
    }

    default void onTerminationMessage(
            @NonNull TerminationMessage request,
            @NonNull Session session) {

    }

}
