package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "utilisateur")
@Data
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @OneToOne
    @JoinColumn(name = "port_monnaie_id", referencedColumnName = "id")
    private PorteMonnaie porteMonnaie;

    @OneToOne
    @JoinColumn(name = "compte_bancaire_id", referencedColumnName = "id")
    private CompteBancaire compteBancaire;
}
