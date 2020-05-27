package org.openvasp.transfer.account;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openvasp.client.VaspInstance;
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

    VaspInstance
            instance1,
            instance2,
            instance3;

    Map<VaspCode, VaspInstance> instanceMap = new HashMap<>();

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

        instance1 = new VaspInstance(module1);
        instance1.setCustomMessageHandler(messageHandler);
        instance1.setCustomErrorHandler(errorLogger);

        instance2 = new VaspInstance(module2);
        instance2.setCustomMessageHandler(messageHandler);
        instance2.setCustomErrorHandler(errorLogger);

        instance3 = new VaspInstance(module3);
        instance3.setCustomMessageHandler(messageHandler);
        instance3.setCustomErrorHandler(errorLogger);

        instanceMap.put(module1.getVaspCode(), instance1);
        instanceMap.put(module2.getVaspCode(), instance2);
        instanceMap.put(module3.getVaspCode(), instance3);

        Thread.sleep(1000);
        log.debug("Vasp instances are ready");
    }

    @AfterEach
    @SneakyThrows
    public void tearDown() {
        instance1.close();
        instance2.close();
        instance3.close();
    }

    public void checkMultipleTransfers() {
        log.debug("checkMultipleTransfers started");

        startTransfer(transferA);
        startTransfer(transferB);
        startTransfer(transferC);

        assertThat(instance1.waitForNoActiveSessions(WAIT_TIMEOUT_1)).isTrue();
        assertThat(instance2.waitForNoActiveSessions(WAIT_TIMEOUT_1)).isTrue();
        assertThat(instance3.waitForNoActiveSessions(WAIT_TIMEOUT_1)).isTrue();

        assertThat(accountService.getBalance(TestConstants.VAAN_1_LIST[0]))
                .isEqualByComparingTo(new BigDecimal("102.0"));

        assertThat(accountService.getBalance(TestConstants.VAAN_2_LIST[0]))
                .isEqualByComparingTo(new BigDecimal("99.0"));

        assertThat(accountService.getBalance(TestConstants.VAAN_3_LIST[0]))
                .isEqualByComparingTo(new BigDecimal("99.0"));
    }

    private void startTransfer(@NonNull final TransferInfo transferInfo) {
        instanceMap
                .get(transferInfo.getOriginator().getVaan().getVaspCode())
                .createOriginatorSession(transferInfo)
                .startTransfer();
    }

}
