package org.openvasp.client.config;

import com.google.inject.AbstractModule;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.openvasp.client.api.whisper.WhisperApi;
import org.openvasp.client.api.whisper.WhisperAsyncApi;
import org.openvasp.client.api.whisper.impl.WhisperApiImpl;
import org.openvasp.client.common.annotation.ContractNode;
import org.openvasp.client.common.annotation.WhisperNode;
import org.openvasp.client.model.VaspCode;
import org.openvasp.client.service.ContractService;
import org.openvasp.client.service.EnsService;
import org.openvasp.client.service.MessageService;
import org.openvasp.client.service.SignService;
import org.openvasp.client.service.impl.ContractServiceImpl;
import org.openvasp.client.service.impl.EnsServiceImpl;
import org.openvasp.client.service.impl.MessageServiceImpl;
import org.openvasp.client.service.impl.SignServiceImpl;
import org.openvasp.client.session.VaspInstance;
import org.openvasp.client.session.impl.VaspInstanceImpl;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Olexandr_Bilovol@epam.com
 */
public class VaspModule extends AbstractModule implements AutoCloseable {

    @Getter
    protected final VaspConfig vaspConfig;

    protected final HttpService whisperHttpService;
    protected final HttpService contractHttpService;

    public VaspModule(@NonNull final VaspConfig vaspConfig) {
        checkNotNull(vaspConfig.getVaspCode());
        checkNotNull(vaspConfig.getVaspInfo());
        checkNotNull(vaspConfig.getVaspInfo().getVaspCode());
        checkNotNull(vaspConfig.getVaspInfo().getPk());
        checkArgument(vaspConfig.getVaspCode().equals(vaspConfig.getVaspInfo().getVaspCode()));

        this.vaspConfig = vaspConfig;

        if (StringUtils.isEmpty(vaspConfig.getWhisperNodeUrl())) {
            vaspConfig.setWhisperNodeUrl(vaspConfig.getHttpServiceUrl());
        }

        if (StringUtils.isEmpty(vaspConfig.getContractNodeUrl())) {
            vaspConfig.setContractNodeUrl(vaspConfig.getHttpServiceUrl());
        }

        this.whisperHttpService = new HttpService(vaspConfig.getWhisperNodeUrl());
        whisperHttpService.addHeader("Accept-Encoding", "identity");

        this.contractHttpService = new HttpService(vaspConfig.getContractNodeUrl());
    }

    public VaspCode getVaspCode() {
        return vaspConfig.getVaspCode();
    }

    @Override
    public void close() throws Exception {
        whisperHttpService.close();
        contractHttpService.close();
    }

    @Override
    protected void configure() {
        super.configure();

        bind(VaspConfig.class).toInstance(vaspConfig);

        bind(Web3jService.class).annotatedWith(WhisperNode.class).toInstance(whisperHttpService);
        bind(Web3jService.class).annotatedWith(ContractNode.class).toInstance(contractHttpService);

        bind(Web3j.class).annotatedWith(WhisperNode.class).toInstance(Web3j.build(whisperHttpService));
        bind(Web3j.class).annotatedWith(ContractNode.class).toInstance(Web3j.build(contractHttpService));

        bind(WhisperApi.class).to(WhisperApiImpl.class);
        bind(WhisperAsyncApi.class).to(WhisperApiImpl.class);

        bindEnsService();
        bindContractService();
        bindSignService();
        bind(MessageService.class).to(MessageServiceImpl.class);
        bind(VaspInstance.class).to(VaspInstanceImpl.class);
    }

    protected void bindEnsService() {
        bind(EnsService.class).to(EnsServiceImpl.class);
    }

    protected void bindContractService() {
        bind(ContractService.class).to(ContractServiceImpl.class);
    }

    protected void bindSignService() {
        bind(SignService.class).to(SignServiceImpl.class);
    }

}
