package org.openvasp.transfer.account;

import lombok.NonNull;
import lombok.val;
import org.openvasp.client.common.Tuple3;
import org.openvasp.client.model.Vaan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.openvasp.client.common.TestConstants.*;

/**
 * @author Olexandr_Bilovol@epam.com
 */
final class AccountService {

    private static final AtomicLong nextTxID = new AtomicLong(1L);

    private final ConcurrentMap<Vaan, String> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, BigDecimal> balance = new ConcurrentHashMap<>();
    private final List<Tuple3<String, String, BigDecimal>> transactions = Collections.synchronizedList(new ArrayList<>());

    AccountService() {
        accounts.put(VAAN_1_LIST[0], "0x10");
        accounts.put(VAAN_1_LIST[1], "0x11");
        accounts.put(VAAN_1_LIST[2], "0x12");

        accounts.put(VAAN_2_LIST[0], "0x20");
        accounts.put(VAAN_2_LIST[1], "0x21");
        accounts.put(VAAN_2_LIST[2], "0x22");

        accounts.put(VAAN_3_LIST[0], "0x30");
        accounts.put(VAAN_3_LIST[1], "0x31");
        accounts.put(VAAN_3_LIST[2], "0x32");

        for (val account : accounts.values()) {
            balance.put(account, BigDecimal.valueOf(100));
        }
    }

    String getAccount(@NonNull final Vaan vaan) {
        return accounts.get(vaan);
    }

    BigDecimal getBalance(@NonNull final Vaan vaan) {
        return balance.get(accounts.get(vaan));
    }

    String add(@NonNull final String account, @NonNull final BigDecimal amount) {
        val txID = String.format("0x%x", nextTxID.getAndIncrement());
        transactions.add(Tuple3.of(account, txID, amount));
        balance.merge(account, amount, BigDecimal::add);
        return txID;
    }

    String subtract(@NonNull final String account, @NonNull final BigDecimal amount) {
        val txID = String.format("0x%x", nextTxID.getAndIncrement());
        transactions.add(Tuple3.of(account, txID, amount.negate()));
        balance.merge(account, amount, BigDecimal::subtract);
        return txID;
    }

    boolean checkTransaction(@NonNull final String txID, @NonNull final BigDecimal amount) {
        return transactions.stream().anyMatch(tx -> txID.equals(tx._2) && amount.equals(tx._3));
    }

}
