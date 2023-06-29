package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Data;

@Entity
@Table(name = "account_paymybuddy")
@Data
public class AccountPayMyBuddy {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;
}

