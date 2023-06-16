package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.model.Utilisateur;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    //go to home page
    @GetMapping("/")
    public String home() {
        return "home";
    }

    //go to registration page
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {

        model.addAttribute("utilisateur", new Utilisateur());
        return "register";
    }

    //go to log in page
    @GetMapping("/logIn")
    public String goToLogIn() {
        return "logIn";
    }
}
