package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "wallet")
@Data
public class Wallet {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "email_user", nullable = false)
    private String userEmail;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private AppUser appUser;
}