package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {

    private final UserService userService;

    //go to registration page
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("appUser", new AppUser());
        return "register";
    }

    @GetMapping("/home")
    public String goToHomePage(Model model, Principal principal) {
        Optional<AppUser> currentUSer = userService.getAppUserByEmail(principal.getName());

        currentUSer.ifPresent(appUser -> model.addAttribute("currentUser", appUser));
        return "home";
    }

    @GetMapping("/transfer")
    public String goToTransferPage(Model model, Principal principal) {
        Optional<AppUser> currentUSer = userService.getAppUserByEmail(principal.getName());

        currentUSer.ifPresent(appUser -> model.addAttribute("currentUser", appUser));
        return "transfer";
    }

    @GetMapping("/profile")
    public String goToProfilePage(Model model, Principal principal) {
        Optional<AppUser> currentUSer = userService.getAppUserByEmail(principal.getName());

        currentUSer.ifPresent(appUser -> model.addAttribute("currentUser", appUser));
        return "profile";
    }

    @PostMapping("/register")
    public String registerAppUser(@ModelAttribute AppUser appUser){

        if (userService.createAppUserAndWallet(appUser) != null) {
            return "registrationSuccessful";
        } else {
            return "registrationFailure";
        }
    }

}
