package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.dto.TransactionForAppUserHistory;
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

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
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
        Optional<AppUser> currentUSer = appUserService.getAppUserByUsername(principal.getName());

        currentUSer.ifPresent(appUser -> model.addAttribute("currentUser", appUser));
        return "home";
    }

    @GetMapping("/transfer")
    public String goToTransferPage(Model model, Principal principal) {
        Optional<AppUser> currentUSer = appUserService.getAppUserByUsername(principal.getName());

        currentUSer.ifPresent(appUser -> {
            model.addAttribute("currentUser", appUser);

            // Fetch the contacts for the current user and add them to the model
            List<AppUser> contacts = appUserService.getContactsForUser(appUser);
            model.addAttribute("contacts", contacts);

            //fetch list of TransactionForAppUserHistory
            List<TransactionForAppUserHistory> transactions = appUserService
                    .getTransactionHistory(principal.getName());
            model.addAttribute("transactions", transactions);
        });
        return "transfer";
    }

    @GetMapping("/profile")
    public String goToProfilePage(Model model, Principal principal) {
        Optional<AppUser> currentUSer = appUserService.getAppUserByUsername(principal.getName());

        currentUSer.ifPresent(appUser -> model.addAttribute("currentUser", appUser));
        return "profile";
    }


    @GetMapping("/contact")
    public String goToContactPage(Model model, Principal principal){

        Optional<AppUser> currentUser = appUserService.getAppUserByUsername(principal.getName());

        currentUser.ifPresent(appUser -> {
            model.addAttribute("currentUser", appUser);
            // Fetch the contacts for the current user and add them to the model
            List<AppUser> contacts = appUserService.getContactsForUser(appUser);
            model.addAttribute("contacts", contacts);

        } );
        return "contact";
    }

    @PostMapping("/register")
    public String registerAppUser(@ModelAttribute AppUser appUser){

        if (appUserService.createAppUser(appUser) != null) {
            return "registrationSuccessful";
        } else {
            return "registrationFailure";
        }
    }

    /*
    @PostMapping("/addContact")
    public String addContact(Principal principal, @RequestParam("contactEmail") String contactEmail) {
        appUserService.addContact(principal.getName(), contactEmail);
        return "redirect:/contact"; // redirect to same page
    }

     */

    @PostMapping("/addContact")
    public String addContact(Principal principal, @RequestParam("contactUsername") String contactUsername) {
        appUserService.addContact(principal.getName(), contactUsername);
        return "redirect:/contact"; // redirect to same page
    }

    @PostMapping("/removeContact")
    public String removeContact(Principal principal, @RequestParam("contactId") Integer contactId) {
        appUserService.removeContact(principal.getName(), contactId);
        return "redirect:contact"; // redirect to same page
    }

    @PostMapping("/transfer")
    public String transferFunds(Principal principal,
                                @RequestParam("contactId") Integer contactId,
                                @RequestParam("amount") BigDecimal amount,
                                @RequestParam(value = "description", required = false) String description) {
        appUserService.transferFunds(principal.getName(), contactId, amount, description);
        return "redirect:/transfer"; // redirect back to the transfer page
    }
}
