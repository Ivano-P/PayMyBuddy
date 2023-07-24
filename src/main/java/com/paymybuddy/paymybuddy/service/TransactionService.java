package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.dto.TransactionForAppUserHistory;
import com.paymybuddy.paymybuddy.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;

public interface TransactionService {
    void saveTransaction(int senderId, int recepientId,
                         BigDecimal amout, BigDecimal transactionFee,
                         Transaction.TransactionType transactionType,
                         Optional<String> description);
    void transferFunds (String appUserUsername, Integer contactId, BigDecimal amount, String description);

    Page<TransactionForAppUserHistory> getTransactionHistory(String username, Pageable pageable);

    void withdrawFunds(String username, BigDecimal amount);

    void genarateTestDepostion(String username);

}
