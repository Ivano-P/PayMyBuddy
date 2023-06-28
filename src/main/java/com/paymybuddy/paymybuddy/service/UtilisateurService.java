package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.PorteMonnaie;
import com.paymybuddy.paymybuddy.model.Utilisateur;
import com.paymybuddy.paymybuddy.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UtilisateurService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Utilisateur createUtilisateurAndPorteMonnaie(Utilisateur utilisateur) {

        //Encode the password
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));

        // Create new PorteMonnaie
        PorteMonnaie porteMonnaie = new PorteMonnaie();
        porteMonnaie.setSolde(BigDecimal.ZERO);
        porteMonnaie.setEmailUtilisateur(utilisateur.getEmail());

        //Associate the porteMonnaie with utilisateur
        utilisateur.setPorteMonnaie(porteMonnaie);

        // Set the default role of the user
        utilisateur.setRole("USER");

        //Set the PorteMonnaie's utilisateur
        porteMonnaie.setUtilisateur(utilisateur);

        //cascade setting saves the porteMonnaie as well
        return utilisateurRepository.save(utilisateur);
    }


    @Transactional(readOnly = true)
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Utilisateur> getUtilisateurById(int id) {
        return utilisateurRepository.findById(id);
    }

    @Transactional
    public Utilisateur updateUtilisateur(Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void deleteUtilisateur(int id) {
        utilisateurRepository.deleteById(id);
    }



    //creat admin user for test, this  is called on startup
    @Transactional
    public Utilisateur creatAdminUtilisateur(){

        Utilisateur adminUtilisateur = new Utilisateur();
        adminUtilisateur.setNom("admin");
        adminUtilisateur.setPrenom("one");
        adminUtilisateur.setEmail("user1");
        adminUtilisateur.setRole("ADMIN");
        adminUtilisateur.setMotDePasse(passwordEncoder.encode("password"));

        // Create new PorteMonnaie
        PorteMonnaie porteMonnaie = new PorteMonnaie();
        porteMonnaie.setSolde(BigDecimal.ZERO);
        porteMonnaie.setEmailUtilisateur(adminUtilisateur.getEmail());

        //Set the PorteMonnaie's utilisateur
        porteMonnaie.setUtilisateur(adminUtilisateur);

        //cascade setting saves the porteMonnaie as well
        return utilisateurRepository.save(adminUtilisateur);
    }

}

