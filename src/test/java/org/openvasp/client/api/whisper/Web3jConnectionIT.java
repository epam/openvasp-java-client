package org.openvasp.client.api.whisper;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openvasp.client.config.RopstenTestModule;
import org.web3j.ens.EnsResolver;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openvasp.client.common.TestConstants.*;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Slf4j
public class Web3jConnectionIT {

    @Test
    @Tag("web3j")
    @SneakyThrows
    public void checkLocalConnection() {
        /*
         * Start
         *    geth --rpcapi personal,db,eth,net,web3 --rpc --dev
         *    OR
         *    npx ganache-cli --deterministic
         * before running this test suite
         * See https://github.com/web3j/web3j
         */
        val web3 = Web3j.build(new HttpService());  // defaults to http://localhost:8545/
        val web3ClientVersion = web3.web3ClientVersion().send();
        val clientVersion = web3ClientVersion.getWeb3ClientVersion();
        log.debug(clientVersion);
        assertThat(clientVersion).matches("^(Geth|EthereumJS).*$");
    }

    @Test
    @Tag("web3j")
    @SneakyThrows
    public void checkRopstenConnection() {
        val web3 = getRopstenConnection();
        val web3ClientVersion = web3.web3ClientVersion().send();
        val clientVersion = web3ClientVersion.getWeb3ClientVersion();

        log.debug(clientVersion);
        assertThat(clientVersion).matches("^(Geth|EthereumJS).*$");
    }

    @Test
    @Tag("web3j")
    @SneakyThrows
    public void checkRopstenEnsEntries() {
        val web3 = getRopstenConnection();
        EnsResolver ens = new EnsResolver(web3);
        assertThat(ens.resolve(CONTRACT_ID_1)).isEqualTo(CONTRACT_ADDRESS_1.toString());
        assertThat(ens.resolve(CONTRACT_ID_2)).isEqualTo(CONTRACT_ADDRESS_2.toString());
        assertThat(ens.resolve(CONTRACT_ID_3)).isEqualTo(CONTRACT_ADDRESS_3.toString());
    }

    private Web3j getRopstenConnection() {
        val infuraEndpoint = RopstenTestModule.infuraConfig.getEndpoint();
        val infuraSecret = ":" + RopstenTestModule.infuraConfig.getSecret();

        val httpService = new HttpService(infuraEndpoint);
        val auth = Base64.toBase64String(infuraSecret.getBytes());
        httpService.addHeader("Authorization", "Basic " + auth);

        return Web3j.build(httpService);
    }


}
