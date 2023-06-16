package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.PorteMonnaie;
import com.paymybuddy.paymybuddy.model.Utilisateur;
import com.paymybuddy.paymybuddy.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    @Autowired
    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional
    public Utilisateur createUtilisateurWithPorteMonnaie(Utilisateur utilisateur) {
        // Create new PorteMonnaie
        PorteMonnaie porteMonnaie = new PorteMonnaie();
        porteMonnaie.setSolde(BigDecimal.ZERO);
        porteMonnaie.setEmailUtilisateur(utilisateur.getEmail());

        //Associate the porteMonnaie with utilisateur
        utilisateur.setPorteMonnaie(porteMonnaie);

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
}

