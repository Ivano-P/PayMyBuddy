package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Data;

@Entity
@Table(name = "transaction")
@Data
public class Transaction {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "expediteur_id", nullable = false)
    private int expediteurId;

    @Column(name = "destinataire_id", nullable = false)
    private int destinataireId;

    @Column(name = "msg")
    private String msg;

    @Column(name = "montant", nullable = false)
    private BigDecimal montant;

    @Column(name = "frais_transaction")
    private BigDecimal fraisTransaction;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_transaction", nullable = false)
    private TypeTransaction typeTransaction;

    @Column(name = "compte_pmb_id", nullable = false)
    private int comptePmbId;

    @ManyToOne
    @JoinColumn(name = "expediteur_id", referencedColumnName = "id")
    private PorteMonnaie expediteur;

    @ManyToOne
    @JoinColumn(name = "destinataire_id", referencedColumnName = "id")
    private PorteMonnaie destinataire;

    @ManyToOne
    @JoinColumn(name = "compte_pmb_id", referencedColumnName = "id")
    private ComptePayMyBuddy comptePayMyBuddy;

    public enum TypeTransaction {
        envoi,
        reception,
        virement_alimentation,
        virement_retrait
    }
}



