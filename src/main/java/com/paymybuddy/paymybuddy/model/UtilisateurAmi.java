package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Entity
@Table(name = "utilisateur_ami")
@Data
public class UtilisateurAmi {

    @EmbeddedId
    private UtilisateurAmiId id;

    @ManyToOne
    @MapsId("utilisateurId")
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne
    @MapsId("amiId")
    @JoinColumn(name = "ami_id")
    private Utilisateur ami;

    @Data
    @Embeddable
    public static class UtilisateurAmiId implements Serializable {
        private int utilisateurId;
        private int amiId;
    }
}

