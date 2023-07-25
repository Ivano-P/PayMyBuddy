package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "app_user")
@Data
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "first_name")
    private String firstName;


    @Column(name = "username", nullable = false, unique = true)
    @NotEmpty(message = "Username should not be empty")
    private String username;

    @Column(name = "email", unique = true)
    @Email(message = "Email should be valid")
    private String email;

    @Column(name = "password")
    @Size(min = 5, message = "Password should have at least 5 characters")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @NotNull(message = "Role should not be empty")
    private Role role = Role.USER;

    @OneToOne(mappedBy = "appUser", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Wallet wallet;

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "bank_account_id", referencedColumnName = "id")
    private BankAccount bankAccount;

    public enum Role {
        USER,
        ADMIN
    }

}
