package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "compte_bancaire")
@Data
public class CompteBancaire {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "intitule", nullable = false)
    private String intitule;

    @Column(name = "iban", nullable = false)
    private String iban;
}

