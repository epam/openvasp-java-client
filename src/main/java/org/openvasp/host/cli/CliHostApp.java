package org.openvasp.host.cli;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.openvasp.client.SimpleTransferHandler;
import org.openvasp.client.VaspClient;
import org.openvasp.client.common.Json;
import org.openvasp.client.common.VaspUtils;
import org.openvasp.client.config.VaspConfig;
import org.openvasp.client.model.*;
import org.openvasp.client.model.TransferMessage.Transaction;
import org.openvasp.client.model.VaspMessage.TypeDescriptor;
import org.openvasp.client.session.OriginatorSession;
import org.openvasp.client.session.Session;

import java.time.ZonedDateTime;

import static java.lang.System.out;

/**
 * @author Olexandr_Bilovol@epam.com
 */
@Slf4j
public final class CliHostApp implements AutoCloseable, SimpleTransferHandler {

    private static final String VASP_CONFIG_PATH = "config/vasp";
    private static final String TRANSFER_DATA_PATH = "config/transfer";

    private final String configNr;
    private final VaspClient vaspClient;

    public CliHostApp(@NonNull final String configNr, @NonNull final String infuraSecret) {
        this.configNr = configNr;

        final VaspConfig vaspConfig = Json.loadFileJson(
                VaspConfig.class,
                VASP_CONFIG_PATH,
                String.format("vasp-config-%s.json", configNr));

        this.vaspClient = new VaspClient(new CliHostModule(vaspConfig, infuraSecret));
        this.vaspClient.setCustomMessageHandler(this);
    }

    @Override
    public void close() {
        vaspClient.shutdown();
        if (!vaspClient.waitForTermination(5000)) {
            vaspClient.close();
        }
    }

    @SneakyThrows
    public void startTerminal() {
        try (final Terminal terminal = TerminalBuilder.terminal()) {
            final LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
            final String prompt = String.format("vasp-%s>", configNr);

            LINE_READER_LOOP:
            while (true) {
                String line = null;
                try {
                    line = lineReader.readLine(prompt);
                    if (StringUtils.isEmpty(line)) {
                        continue;
                    }

                    String[] command = line.trim().split("\\s+");
                    switch (command[0]) {
                        case "exit":
                            break LINE_READER_LOOP;

                        case "start":
                            if (command.length > 1 && StringUtils.isNotEmpty(command[1])) {
                                transfer(command[1]);
                            }
                            break;

                        default:
                            break;
                    }
                } catch (UserInterruptException e) {
                    // Ignore
                } catch (EndOfFileException e) {
                    return;
                }
            }
        }
    }

    public void transfer(@NonNull final String transferName) {
        final TransferInfo transferInfo = Json.loadFileJson(
                TransferInfo.class,
                TRANSFER_DATA_PATH,
                String.format("transfer-info-%s.json", transferName));

        final OriginatorSession session = vaspClient.createOriginatorSession(transferInfo);
        session.startTransfer();
    }

    @Override
    public void accept(@NonNull final VaspMessage message, @NonNull final Session session) {
        logMessage(message.getVaspInfo().getVaspCode(), message);
        SimpleTransferHandler.super.accept(message, session);
    }

    @Override
    public void onTransferRequest(
            @NonNull final TransferRequest request,
            @NonNull final TransferReply response,
            @NonNull final Session session) {

        final String destAddr = "0x" + session.transferInfo().getBeneficiary().getVaan().getData();
        response.getTransfer().setDestinationAddress(destAddr);
        SimpleTransferHandler.super.onTransferRequest(request, response, session);
    }

    @Override
    public void onTransferReply(
            @NonNull final TransferReply request,
            @NonNull final TransferDispatch response,
            @NonNull final Session session) {

        Transaction tx = new Transaction();
        response.setTx(tx);
        tx.setId(VaspUtils.randomHexStr(16));
        tx.setDateTime(ZonedDateTime.now());
        SimpleTransferHandler.super.onTransferReply(request, response, session);
    }

    @Override
    public void onTransferDispatch(
            @NonNull final TransferDispatch request,
            @NonNull final TransferConfirmation response,
            @NonNull final Session session) {

        response.getHeader().setResponseCode(VaspResponseCode.OK.id);
        SimpleTransferHandler.super.onTransferDispatch(request, response, session);
    }

    private void logMessage(
            @NonNull final VaspCode vaspCode,
            @NonNull final VaspMessage vaspMessage) {

        final TypeDescriptor messageType = vaspMessage.getHeader().getMessageType();
        out.format("\n%s from %s\n", messageType, vaspCode);
        switch (messageType) {
            case TRANSFER_REQUEST: {
                final TransferRequest message = vaspMessage.asTransferRequest();
                out.format(
                        "Originator %s(%s)\n",
                        message.getOriginator().getVaan(),
                        message.getOriginator().getName());
                out.format(
                        "Beneficiary %s(%s)\n",
                        message.getBeneficiary().getVaan(),
                        message.getBeneficiary().getName());
                out.format(
                        "Amount %s\n", message.getTransfer().getAmount());
                break;
            }

            case TRANSFER_DISPATCH: {
                final TransferDispatch message = vaspMessage.asTransferDispatch();
                out.format(
                        "Originator %s(%s)\n",
                        message.getOriginator().getVaan(),
                        message.getOriginator().getName());
                out.format(
                        "Beneficiary %s(%s)\n",
                        message.getBeneficiary().getVaan(),
                        message.getBeneficiary().getName());
                out.format(
                        "Amount %s\n", message.getTransfer().getAmount());
                out.format(
                        "TxID %s\n", message.getTx().getId());
                break;
            }
        }
    }


    public static void main(String[] args) throws Exception {
        final Options opts = new Options();
        opts.addRequiredOption("c", "config", true, "Configuration number");
        opts.addRequiredOption("s", "infura-secret", true, "Infura secret");

        final CommandLine cmdLine = new DefaultParser().parse(opts, args);
        if (!cmdLine.hasOption('c')) {
            out.println("--config parameter required");
            return;
        }

        if (!cmdLine.hasOption('s')) {
            out.println("--infura-secret parameter required");
            return;
        }

        try (final CliHostApp app = new CliHostApp(cmdLine.getOptionValue('c'), cmdLine.getOptionValue('s'))) {
            app.startTerminal();
        }
    }

}
