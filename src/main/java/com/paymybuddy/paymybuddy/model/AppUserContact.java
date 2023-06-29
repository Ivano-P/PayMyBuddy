package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Entity
@Table(name = "app-user_contact")
@Data
public class AppUserContact {

    @EmbeddedId
    private appUserContactId id;

    @ManyToOne
    @MapsId("app-user_id")
    @JoinColumn(name = "app-user_id")
    private AppUser appUser;

    @ManyToOne
    @MapsId("contactId")
    @JoinColumn(name = "contact_id")
    private AppUser Contact;

    @Data
    @Embeddable
    public static class appUserContactId implements Serializable {
        private int appUserId;
        private int contactId;
    }
}

