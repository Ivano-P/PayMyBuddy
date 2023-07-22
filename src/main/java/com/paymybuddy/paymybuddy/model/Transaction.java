package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Entity
@Table(name = "transaction")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "sender_id", nullable = false)
    private int senderId;

    @Column(name = "recepient_id", nullable = false)
    private int recepientId;

    @Column(name = "description", length = 50)
    private String description;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "time_stamp")
    private LocalDateTime timeStamp;

    @Column(name = "transaction_fee")
    private BigDecimal transactionFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @ManyToOne
    @JoinColumn(name = "sender_id", insertable = false, updatable = false)
    private Wallet sender;

    @ManyToOne
    @JoinColumn(name = "recepient_id", insertable = false, updatable = false)
    private Wallet recepient;

    public enum TransactionType {
        SEND,
        RECEIVE,
        DEPOSIT,
        WITHDRAWAL
    }
}



