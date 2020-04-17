package org.openvasp.transfer.account;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openvasp.client.VaspClient;
import org.openvasp.client.common.Json;
import org.openvasp.client.common.TestConstants;
import org.openvasp.client.common.VaspException;
import org.openvasp.client.config.VaspModule;
import org.openvasp.client.model.TransferInfo;
import org.openvasp.client.model.VaspCode;
import org.openvasp.client.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openvasp.client.common.TestConstants.WAIT_TIMEOUT_1;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public abstract class BaseAccountTransferIT {

    private final Logger log = LoggerFactory.getLogger(getClass());

    final VaspModule
            module1,
            module2,
            module3;

    VaspClient
            client1,
            client2,
            client3;

    Map<VaspCode, VaspClient> clientMap = new HashMap<>();

    AccountService accountService;

    final TransferInfo
            transferA = Json.loadTestJson(TransferInfo.class, "transfer/account/transfer-info-a.json"),
            transferB = Json.loadTestJson(TransferInfo.class, "transfer/account/transfer-info-b.json"),
            transferC = Json.loadTestJson(TransferInfo.class, "transfer/account/transfer-info-c.json");

    public BaseAccountTransferIT(
            @NonNull final VaspModule module1,
            @NonNull final VaspModule module2,
            @NonNull final VaspModule module3) {

        this.module1 = module1;
        this.module2 = module2;
        this.module3 = module3;
    }

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        this.accountService = new AccountService();
        val messageHandler = new AccountTransferHandler(accountService);
        final BiConsumer<VaspException, Session> errorLogger = 
                (exception, session) -> { log.error("Error while executing scenario: ", exception); };

        client1 = new VaspClient(module1);
        client1.setCustomMessageHandler(messageHandler);
        client1.setCustomErrorHandler(errorLogger);

        client2 = new VaspClient(module2);
        client2.setCustomMessageHandler(messageHandler);
        client2.setCustomErrorHandler(errorLogger);

        client3 = new VaspClient(module3);
        client3.setCustomMessageHandler(messageHandler);
        client3.setCustomErrorHandler(errorLogger);

        clientMap.put(module1.getVaspCode(), client1);
        clientMap.put(module2.getVaspCode(), client2);
        clientMap.put(module3.getVaspCode(), client3);

        Thread.sleep(1000);
        log.debug("Vasp clients are ready");
    }

    @AfterEach
    @SneakyThrows
    public void tearDown() {
        client1.close();
        client2.close();
        client3.close();
    }

    public void checkMultipleTransfers() {
        log.debug("checkMultipleTransfers started");

        startTransfer(transferA);
        startTransfer(transferB);
        startTransfer(transferC);

        assertThat(client1.waitForNoActiveSessions(WAIT_TIMEOUT_1)).isTrue();
        assertThat(client2.waitForNoActiveSessions(WAIT_TIMEOUT_1)).isTrue();
        assertThat(client3.waitForNoActiveSessions(WAIT_TIMEOUT_1)).isTrue();

        assertThat(accountService.getBalance(TestConstants.VAAN_1_LIST[0]))
                .isEqualByComparingTo(new BigDecimal("102.0"));

        assertThat(accountService.getBalance(TestConstants.VAAN_2_LIST[0]))
                .isEqualByComparingTo(new BigDecimal("99.0"));

        assertThat(accountService.getBalance(TestConstants.VAAN_3_LIST[0]))
                .isEqualByComparingTo(new BigDecimal("99.0"));
    }

    private void startTransfer(@NonNull final TransferInfo transferInfo) {
        clientMap
                .get(transferInfo.getOriginator().getVaan().getVaspCode())
                .createOriginatorSession(transferInfo)
                .startTransfer();
    }

}
