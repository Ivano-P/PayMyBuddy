package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.service.AppUserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Optional;

@Controller
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {

    private final AppUserService appUserService;

    //go to registration page
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("appUser", new AppUser());
        return "register";
    }

    @GetMapping("/home")
    public String goToHomePage(Model model, Principal principal) {
        Optional<AppUser> currentUSer = appUserService.getAppUserByEmail(principal.getName());

        currentUSer.ifPresent(appUser -> model.addAttribute("currentUser", appUser));
        return "home";
    }

    @GetMapping("/transfer")
    public String goToTransferPage(Model model, Principal principal) {
        Optional<AppUser> currentUSer = appUserService.getAppUserByEmail(principal.getName());

        currentUSer.ifPresent(appUser -> model.addAttribute("currentUser", appUser));
        return "transfer";
    }

    @GetMapping("/profile")
    public String goToProfilePage(Model model, Principal principal) {
        Optional<AppUser> currentUSer = appUserService.getAppUserByEmail(principal.getName());

        currentUSer.ifPresent(appUser -> model.addAttribute("currentUser", appUser));
        return "profile";
    }

    //TODO:add list of contact to this page.
    @GetMapping("/contact")
    public String goToContactPage(Model model, Principal principal){

        Optional<AppUser> currentUser = appUserService.getAppUserByEmail(principal.getName());

        currentUser.ifPresent(appUser -> model.addAttribute("currentUser", appUser));
        return "contact";
    }

    @PostMapping("/register")
    public String registerAppUser(@ModelAttribute AppUser appUser){

        if (appUserService.createAppUserAndWallet(appUser) != null) {
            return "registrationSuccessful";
        } else {
            return "registrationFailure";
        }
    }

    @PostMapping("/addContact")
    public String addContact(Principal principal, @RequestParam("contactEmail") String contactEmail) {
        String userEmail = principal.getName(); // get the email of the currently logged in user
        appUserService.addContact(userEmail, contactEmail);
        return "redirect:/contact"; // to same page
    }

}
