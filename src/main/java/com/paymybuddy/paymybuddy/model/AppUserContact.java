package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Entity
@Table(name = "app_user_contact")
@Data
public class AppUserContact {

    @EmbeddedId
    private AppUserContactId id;

    @ManyToOne
    @MapsId("appUserId")
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    @ManyToOne
    @MapsId("contactId")
    @JoinColumn(name = "contact_id")
    private AppUser Contact;

    @Data
    @Embeddable
    public static class AppUserContactId implements Serializable {
        private int appUserId;
        private int contactId;
    }
}

