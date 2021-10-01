package org.openvasp.client.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openvasp.client.common.VaspException;
import org.openvasp.client.model.EthAddr;
import org.web3j.ens.EnsResolutionException;
import org.web3j.ens.EnsResolver;
import org.web3j.protocol.Web3j;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EnsServiceImplTests {

    private EnsServiceImpl ensService;
    private String ethAddress;

    @Mock
    EnsResolver ensResolver;
    @Mock
    Web3j web3j;


    @BeforeEach
    public void init() throws NoSuchFieldException, IllegalAccessException {
        ensService = new EnsServiceImpl(web3j);
        ethAddress = "0x6befaf0656b953b188a0ee3bf3db03d07dface61";
        Field field = EnsServiceImpl.class.getDeclaredField("ensResolver");
        field.setAccessible(true);
        field.set(ensService, ensResolver);
    }

    @Test
    public void resolveContractAddressTest() {
        when(ensResolver.resolve(any())).thenReturn(ethAddress);
        Assertions.assertEquals(new EthAddr(ethAddress), ensService.resolveContractAddress(ethAddress));
    }

    @Test
    public void resolveContractAddressNullTest() {
        when(ensResolver.resolve(any())).thenThrow(EnsResolutionException.class);
        Assertions.assertThrows(VaspException.class, () -> ensService.resolveContractAddress(ethAddress));
    }
}
