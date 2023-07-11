package com.paymybuddy.paymybuddy.dto;

import com.paymybuddy.paymybuddy.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransactionForAppUserHistory {
    String contactUsername;
    String description;
    BigDecimal amount;
    Transaction.TransactionType transactionType;
}
