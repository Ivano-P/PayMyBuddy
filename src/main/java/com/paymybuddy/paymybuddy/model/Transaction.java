package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "Sender id should not be empty")
    private int senderId;

    @Column(name = "recepient_id", nullable = false)
    @NotNull(message = "recipient id should not be empty")
    private int recepientId;

    @Column(name = "description", length = 50)
    private String description;

    @Column(name = "amount", nullable = false)
    @NotNull(message = "amount should not be empty")
    private BigDecimal amount;

    @Column(name = "time_stamp")
    @NotNull(message = "time stamp should not be empty")
    private LocalDateTime timeStamp;

    @Column(name = "transaction_fee")
    private BigDecimal transactionFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    @NotNull(message = "transaction type should not be empty")
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



