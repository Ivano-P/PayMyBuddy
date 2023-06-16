package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.model.Utilisateur;
import com.paymybuddy.paymybuddy.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @Autowired
    public UtilisateurController(UtilisateurService utilisateurService){
        this.utilisateurService = utilisateurService;
    }

    @GetMapping("/myAccount")
    public String goToMyAccount() {
        return "myAccount";
    }

    @PostMapping("/register")
    public String registerUtilisateur(@ModelAttribute Utilisateur utilisateur){

        if (utilisateurService.createUtilisateurWithPorteMonnaie(utilisateur) != null) {
            return "registrationSuccessful";
        } else {
            return "registrationFailure";
        }
    }

}
