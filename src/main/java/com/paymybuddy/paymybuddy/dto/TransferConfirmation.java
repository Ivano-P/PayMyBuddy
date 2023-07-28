package com.paymybuddy.paymybuddy.dto;

import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransferConfirmation {
    String sender;
    Transaction.TransactionType type;
    AppUser recipient;
    String description;
    BigDecimal transferAmount;
    BigDecimal transferFee;
    BigDecimal amountPlusFee;
    BigDecimal balanceAfterTransfer;
}
