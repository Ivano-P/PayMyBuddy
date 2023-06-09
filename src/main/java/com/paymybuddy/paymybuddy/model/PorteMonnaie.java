package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "porte_monnaie")
@Data
public class PorteMonnaie {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "solde")
    private BigDecimal solde;

    @Column(name = "email_utilisateur", nullable = false)
    private String emailUtilisateur;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "id")
    private Utilisateur utilisateur;
}
