package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.BankAccount;
import com.paymybuddy.paymybuddy.service.AppUserService;
import com.paymybuddy.paymybuddy.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Log4j2
public class UserController {

    private final AppUserService appUserService;
    private final BankAccountService bankAccountService;

    private static final String REDIRECT_PROFILE = "redirect:/profile";
    private static final String CURRENT_USER = "currentUser";

    private AppUser getAppUserService(String username){
        log.info("getAppUserService method called");
        Optional<AppUser> currentUserOptional = appUserService.getAppUserByUsername(username);
        return currentUserOptional.orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    //go to log in page
    @GetMapping("/logIn")
    public String goToLogIn() {
        log.info("goToLogIn method called");
        return "logIn";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        log.info("showRegistrationForm method called");
        model.addAttribute("appUser", new AppUser());
        return "register";
    }

    @GetMapping("/home")
    public String goToHomePage(Model model, Principal principal) {
        log.info("goToHomePage method called for user {} ", principal.getName());
        AppUser currentAppUser = getAppUserService(principal.getName());
        model.addAttribute(CURRENT_USER, currentAppUser);
        this.appUserService.checkIfAllUserInfoPresent(currentAppUser);
        return "home";
    }

    @GetMapping("/profile")
    public String goToProfilePage(Model model, Principal principal) {
        log.info("goToProfilePage method called for user {} ", principal.getName());
        AppUser currentAppUser = getAppUserService(principal.getName());

        model.addAttribute(CURRENT_USER, currentAppUser);
        appUserService.checkIfAllUserInfoPresent(currentAppUser);
        boolean hasBankAccount = bankAccountService.hasBankAccount(principal.getName());

        model.addAttribute("hasBankAccount", hasBankAccount);
        if (hasBankAccount) {
            BankAccount bankAccount = bankAccountService
                    .getAppUserBankAccount(currentAppUser.getId());

            model.addAttribute("bankAccount", bankAccount);
        }

        return "profile";
    }

    @GetMapping("/contact")
    public String goToContactPage(Model model, Principal principal) {
        log.info("goToContactPage method called for user {} ", principal.getName());
        AppUser currentAppUser = getAppUserService(principal.getName());

        model.addAttribute(CURRENT_USER, currentAppUser);
        appUserService.checkIfAllUserInfoPresent(currentAppUser);

        // Fetch the contacts for the current user and add them to the model
        List<AppUser> contacts = appUserService.getContactsForUser(currentAppUser);
        model.addAttribute("contacts", contacts);

        return "contact";
    }

    @GetMapping("update_profile")
    public String goToUpdateProfileInfoPage(Model model, Principal principal) {
        log.info("goToUpdateProfileInfoPage method called for user {} ", principal.getName());
        AppUser currentAppUser = getAppUserService(principal.getName());

        model.addAttribute(CURRENT_USER, currentAppUser);
        model.addAttribute("appUser", new AppUser());

        return "update_profile";
    }

    @PostMapping("/register")
    public String registerAppUser(@Valid @ModelAttribute AppUser appUser, BindingResult bindingResult) {
        log.info("registerAppUser method called");

        if (bindingResult.hasErrors()) {
            return "registrationFailure";
        }

        if (appUserService.createAppUser(appUser) != null) {
            return "registrationSuccessful";
        } else {
            return "registrationFailure";
        }
    }

    @PostMapping("/addContact")
    public String addContact(Principal principal, @RequestParam("contactUsername") String contactUsername) {
        log.info("addContact method called for user {} ", principal.getName());
        appUserService.addContact(principal.getName(), contactUsername);
        return "redirect:/contact"; // redirect to same page
    }

    @PostMapping("/removeContact")
    public String removeContact(Principal principal, @RequestParam("contactId") Integer contactId) {
        log.info("removeContact method called for user {} ", principal.getName());
        appUserService.removeContact(principal.getName(), contactId);
        return "redirect:/contact"; // redirect to same page
    }


    @PostMapping("/updateProfileInfo")
    public String updateProfileInfo(@ModelAttribute AppUser updatedUser, Principal principal) {
        log.info("updateProfileInfo method called for user {} ", principal.getName());
        AppUser currentAppUser = getAppUserService(principal.getName());

        currentAppUser.setFirstName(updatedUser.getFirstName());
        currentAppUser.setLastName(updatedUser.getLastName());
        currentAppUser.setEmail(updatedUser.getEmail());

        appUserService.updateAppUser(currentAppUser);

        return "redirect:/profile?updateSuccess=true";
    }

    //TODO: UT
    @PostMapping("/updatePassword")
    public String updatePassword(Principal principal,
                                 @RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword){

        log.info("updatePassword method called for user {} ", principal.getName());
        AppUser currentAppUser = getAppUserService(principal.getName());

        appUserService.updateUserPassword(currentAppUser, currentPassword, newPassword, confirmPassword);
        appUserService.updateAppUser(currentAppUser);
        return "redirect:/profile?updateSuccess=true";
    }
}
