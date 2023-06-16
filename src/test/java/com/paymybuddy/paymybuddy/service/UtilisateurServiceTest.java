package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.CompteBancaire;
import com.paymybuddy.paymybuddy.model.PorteMonnaie;
import com.paymybuddy.paymybuddy.model.Utilisateur;
import com.paymybuddy.paymybuddy.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UtilisateurServiceTest {
    @MockBean
    private UtilisateurRepository utilisateurRepository;

    private UtilisateurService utilisateurService;

    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        utilisateurRepository = Mockito.mock(UtilisateurRepository.class);
        utilisateurService = new UtilisateurService(utilisateurRepository);

        utilisateur = new Utilisateur();
        utilisateur.setNom("DUPONT");
        utilisateur.setPrenom("JEAN");
        utilisateur.setEmail("test@test.com");
        utilisateur.setMotDePasse("testPassword");

        PorteMonnaie porteMonnaie = new PorteMonnaie();
        porteMonnaie.setId(1);
        porteMonnaie.setSolde(new BigDecimal("1000.00"));
        porteMonnaie.setEmailUtilisateur(utilisateur.getEmail());
        utilisateur.setPorteMonnaie(porteMonnaie);

        CompteBancaire compteBancaire = new CompteBancaire();
        compteBancaire.setId(1);
        compteBancaire.setIntitule("Test Account");
        compteBancaire.setIban("FR7630004000031234567890143");
        utilisateur.setCompteBancaire(compteBancaire);
    }

    @Test
     void testCreateUtilisateur() {
        when(utilisateurRepository.save(utilisateur)).thenReturn(utilisateur);
        assertEquals(utilisateur, utilisateurService.createUtilisateurWithPorteMonnaie(utilisateur));
    }

    @Test
     void testGetAllUtilisateurs() {
        Utilisateur utilisateur1 = utilisateur;
        Utilisateur utilisateur2 = new Utilisateur();
        utilisateur2.setNom("GEORGE");
        utilisateur2.setPrenom("Jeanne");
        utilisateur2.setEmail("test@test.com");
        utilisateur2.setMotDePasse("testPassword");
        when(utilisateurRepository.findAll()).thenReturn(Arrays.asList(utilisateur1, utilisateur2));
        assertEquals(2, utilisateurService.getAllUtilisateurs().size());
    }

    @Test
    void testGetUtilisateurById() {
        utilisateur.setId(1);
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(utilisateur));
        assertEquals(Optional.of(utilisateur), utilisateurService.getUtilisateurById(1));
    }

    @Test
    void testUpdateUtilisateur() {
        when(utilisateurRepository.save(utilisateur)).thenReturn(utilisateur);
        assertEquals(utilisateur, utilisateurService.updateUtilisateur(utilisateur));
    }

    @Test
    void testDeleteUtilisateur() {
        int id = 1;
        utilisateurService.deleteUtilisateur(id);
        verify(utilisateurRepository, times(1)).deleteById(id);
    }
}

