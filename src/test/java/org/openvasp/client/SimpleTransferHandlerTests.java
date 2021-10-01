package org.openvasp.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openvasp.client.model.*;
import org.openvasp.client.session.Session;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SimpleTransferHandlerTests {

    @Mock
    private Session session;

    private SimpleTransferHandler handler;

    @Test
    public void acceptTest() {
        handler = new SimpleTransferHandlerImpl();

        VaspMessage vaspMessage = new SessionRequest();
        handler.accept(vaspMessage, session);

        vaspMessage = new SessionReply();
        TransferInfo transferInfo = createTransferInfo();
        when(session.transferInfo()).thenReturn(transferInfo);
        handler.accept(vaspMessage, session);

        vaspMessage = new TransferRequest();
        handler.accept(vaspMessage, session);

        vaspMessage = new TransferReply();
        handler.accept(vaspMessage, session);

        vaspMessage = new TransferDispatch();
        handler.accept(vaspMessage, session);

        vaspMessage = new TransferConfirmation();
        handler.accept(vaspMessage, session);

        vaspMessage = new TerminationMessage();
        handler.accept(vaspMessage, session);

        verify(session, times(6)).sendMessage(any());
        verify(session, times(2)).remove();
    }

    private TransferInfo createTransferInfo() {
        TransferInfo transferInfo = new TransferInfo();
        transferInfo.setBeneficiary(new Beneficiary("Average Joe", new Vaan("10007dface610000000001e7")));
        transferInfo.setOriginator(new Originator());
        transferInfo.setTx(new TransferDispatch.Tx());
        transferInfo.setTransfer(new TransferRequest.Transfer());
        transferInfo.setDestinationAddress("destinationAddress");
        return transferInfo;
    }

    private class SimpleTransferHandlerImpl implements SimpleTransferHandler {}
}
