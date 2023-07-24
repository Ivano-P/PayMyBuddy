package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.BankAccount;
import com.paymybuddy.paymybuddy.service.AppPmbService;
import com.paymybuddy.paymybuddy.service.AppUserService;
import com.paymybuddy.paymybuddy.service.BankAccountService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Optional;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@Log4j2
@Controller
public class BankAccountController {
    private final AppUserService appUserService;
    private final AppPmbService appPmbService;
    private final BankAccountService bankAccountService;

    private static final String REDIRECT_PROFILE = "redirect:/profile";
    private static final String REDIRECT_TRANSFER = "redirect:/transfer";
    private static final String CURRENT_USER = "currentUser";

    private AppUser getAppUserService(String username){
        log.info("getAppUserService method called");
        Optional<AppUser> currentUserOptional = appUserService.getAppUserByUsername(username);
        return currentUserOptional.orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @GetMapping("/iban")
    public String goToIban(Model model, Principal principal) {
        log.info("goToIban method called for user {} ", principal.getName());
        AppUser currentAppUser = getAppUserService(principal.getName());
        model.addAttribute(CURRENT_USER, currentAppUser);

        String iban = appPmbService.getPmbIban();
        model.addAttribute("iban", iban);

        return "iban";
    }

    @PostMapping("/addBankAccount")
    public String addBankAccount(Principal principal,
                                 @RequestParam("lastName") String lastName,
                                 @RequestParam("firstName") String firstName,
                                 @RequestParam("iban") String iban) {
        log.info("addBankAccount method called for user {} ", principal.getName());
        //check bank account validity
        BankAccount bankAccountToAdd = bankAccountService
                .checkBankAccountValidity(principal
                        .getName(), lastName, firstName, iban);

        //add/update the appUsers bank account.
        bankAccountService.updateBankAccount(bankAccountToAdd);
        return REDIRECT_PROFILE;
    }

    @PostMapping("/removeBankAccount")
    public String removeBankAccount(@RequestParam("appUserId") Integer appUserId) {
        log.info("removeBankAccount method called");
        bankAccountService.removeBankAccount(appUserId);

        return REDIRECT_PROFILE;
    }

    @PostMapping("/noAccountForWithdrawal")
    public String indicateAddBankAccount() {
        log.info("indicatedAddBankAccount method called");
        bankAccountService.noBankAccountForWithdrawal();
        return REDIRECT_TRANSFER;
    }

}
