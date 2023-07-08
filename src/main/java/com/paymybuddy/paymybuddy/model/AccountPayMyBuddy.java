package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
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
}

