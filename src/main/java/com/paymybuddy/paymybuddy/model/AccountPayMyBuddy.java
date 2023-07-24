package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "account_paymybuddy")
@Data
public class AccountPayMyBuddy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "transaction_fee")
    private double transactionFee = 0.005;

    @Column(name = "iban", nullable = false, length = 34)
    @Size(min = 5, max = 32, message = "Iban should have between 22 and 34 characters")
    private String iban = "FR345678911234567892123";
}

