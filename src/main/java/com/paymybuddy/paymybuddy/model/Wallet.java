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

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private AppUser appUser;
}
