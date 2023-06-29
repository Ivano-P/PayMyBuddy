package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.model.Utilisateur;
import com.paymybuddy.paymybuddy.service.UtilisateurService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    //go to registration page
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {

        model.addAttribute("utilisateur", new Utilisateur());
        return "register";
    }

    @GetMapping("/myAccount")
    public String goToMyAccount() {
        return "myAccount";
    }

    @PostMapping("/register")
    public String registerUtilisateur(@ModelAttribute Utilisateur utilisateur){

        if (utilisateurService.createUtilisateurAndPorteMonnaie(utilisateur) != null) {
            return "registrationSuccessful";
        } else {
            return "registrationFailure";
        }
    }

}
