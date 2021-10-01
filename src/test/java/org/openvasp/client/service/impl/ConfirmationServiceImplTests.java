package org.openvasp.client.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openvasp.client.config.VaspConfig;
import org.openvasp.client.model.*;
import org.openvasp.client.service.ContractService;
import org.openvasp.client.service.VaspIdentityService;
import org.openvasp.client.service.WhisperService;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConfirmationServiceImplTests {

    @Mock
    private WhisperService whisperService;
    @Mock
    private ContractService contractService;
    @Mock
    private VaspIdentityService vaspIdentityService;

    private VaspMessage vaspMessage;
    private VaspConfig vaspConfig;
    private VaspContractInfo vaspContractInfo;
    private ConfirmationServiceImpl confirmationService;

    @BeforeEach
    public void init() {
        EthAddr ethAddr = new EthAddr("0x6befaf0656b953b188a0ee3bf3db03d07dface61");
        VaspInfo vaspInfo = new VaspInfo();
        vaspInfo.setVaspId(ethAddr);

        vaspConfig = new VaspConfig();
        vaspConfig.setAcknowledgmentEnabled(true);
        vaspConfig.setVaspCode(new VaspCode("7dface61"));
        vaspConfig.setHandshakePrivateKey("0xe7578145d518e5272d660ccfdeceedf2d55b90867f2b7a6e54dc726662aebac2");
        vaspConfig.setVaspInfo(vaspInfo);

        vaspContractInfo = new VaspContractInfo();
        vaspContractInfo.setHandshakeKey("0x044ffeb548a05b879757dc37911bb41f95966311b00b8687da8913c6b0bf7deddd1c233d8294e0c31f0099058bf056c0f5fb8919e87bf148b0f04ace8f19004bb9");

        vaspMessage = new TransferConfirmation();
        VaspMessage.Header header = new VaspMessage.Header();
        header.setMessageId("0x89a4de97f11aa156372a4f39cdadf194");
        vaspMessage.setHeader(header);

        confirmationService = new ConfirmationServiceImpl(vaspConfig, whisperService, contractService, vaspIdentityService);
    }

    @Test
    public void registerForConfirmationTest() {
        when(whisperService.addTopicListener(any(), any(), any(), any())).thenReturn(1L);
        confirmationService.registerForConfirmation(vaspMessage);
        verify(whisperService).addTopicListener(any(), any(), any(), any());
    }

    @Test
    public void confirmReceiptTest() {
        EthAddr ethAddr = new EthAddr("0x6befaf0656b953b188a0ee3bf3db03d07dface61");
        when(vaspIdentityService.resolveSenderVaspId(vaspMessage)).thenReturn(Optional.of(ethAddr));
        when(contractService.getVaspContractInfo(ethAddr)).thenReturn(vaspContractInfo);
        confirmationService.confirmReceipt(vaspMessage);
        verify(whisperService).send(any(), any(), any(), any());
    }

    @Test
    public void confirmationDisabledTest() {
        vaspConfig.setAcknowledgmentEnabled(false);
        confirmationService = new ConfirmationServiceImpl(vaspConfig, whisperService, contractService, vaspIdentityService);

        confirmationService.confirmReceipt(vaspMessage);
        verify(whisperService, never()).send(any(), any(), any(), any());

        confirmationService.registerForConfirmation(vaspMessage);
        verify(whisperService, never()).addTopicListener(any(), any(), any(), any());
    }
}
