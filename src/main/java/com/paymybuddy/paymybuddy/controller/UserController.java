package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.dto.TransactionForAppUserHistory;
import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.BankAccount;
import com.paymybuddy.paymybuddy.service.AppPmbService;
import com.paymybuddy.paymybuddy.service.AppUserService;
import com.paymybuddy.paymybuddy.service.BankAccountService;
import com.paymybuddy.paymybuddy.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final BankAccountService bankAccountService;
    private final AppPmbService appPmbService;
    private final TransactionService transactionService;


    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("appUser", new AppUser());
        return "register";
    }

    @GetMapping("/home")
    public String goToHomePage(Model model, Principal principal) {
        Optional<AppUser> currentUSer = appUserService.getAppUserByUsername(principal.getName());

        currentUSer.ifPresent(appUser -> {
            model.addAttribute("currentUser", appUser);
            appUserService.checkIfAllUserInfoPresent(appUser);
        });

        return "home";
    }

    @GetMapping("/transfer")
    public String goToTransferPage(Model model, Principal principal, @RequestParam(defaultValue = "0") int page) {
        Optional<AppUser> currentUSer = appUserService.getAppUserByUsername(principal.getName());
        PageRequest pageRequest = PageRequest.of(page, 5);

        currentUSer.ifPresent(appUser -> {
            model.addAttribute("currentUser", appUser);
            appUserService.checkIfAllUserInfoPresent(appUser);

            // Fetch the contacts for the current user and add them to the model
            List<AppUser> contacts = appUserService.getContactsForUser(appUser);
            model.addAttribute("contacts", contacts);

            boolean hasBankAccount = bankAccountService.hasBankAccount(principal.getName());

            model.addAttribute("hasBankAccount", hasBankAccount);

            //fetch list of TransactionForAppUserHistory
            Page<TransactionForAppUserHistory> transactions = transactionService
                    .getTransactionHistory(principal.getName(), pageRequest);
            model.addAttribute("transactions", transactions);

            int totalPages = transactions.getTotalPages();
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("currentPage", page);
        });
        return "transfer";
    }

    @GetMapping("/iban")
    public String goToIban(Model model, Principal principal) {
        Optional<AppUser> currentUSer = appUserService.getAppUserByUsername(principal.getName());
        currentUSer.ifPresent(appUser -> model.addAttribute("currentUser", appUser));

        String iban = appPmbService.getPmbIban();
        model.addAttribute("iban", iban);

        return "iban";
    }

    @PostMapping("/deposit")
    public String depositFunds() {
        boolean showIban = true;
        bankAccountService.showIbanForDeposit(showIban);

        return "redirect:/iban";
    }

    @GetMapping("/profile")
    public String goToProfilePage(Model model, Principal principal) {
        Optional<AppUser> appUserOptional = appUserService.getAppUserByUsername(principal.getName());
        appUserOptional.ifPresent(appUser -> {
            model.addAttribute("currentUser", appUser);
            appUserService.checkIfAllUserInfoPresent(appUser);
            boolean hasBankAccount = bankAccountService.hasBankAccount(principal.getName());

            model.addAttribute("hasBankAccount", hasBankAccount);
            if (hasBankAccount) {
                BankAccount bankAccount = bankAccountService
                        .getAppUserBankAccount(appUser.getId());

                model.addAttribute("bankAccount", bankAccount);
            }

        });


        return "profile";
    }


    @GetMapping("/contact")
    public String goToContactPage(Model model, Principal principal) {

        Optional<AppUser> currentUser = appUserService.getAppUserByUsername(principal.getName());

        currentUser.ifPresent(appUser -> {
            model.addAttribute("currentUser", appUser);
            appUserService.checkIfAllUserInfoPresent(appUser);

            // Fetch the contacts for the current user and add them to the model
            List<AppUser> contacts = appUserService.getContactsForUser(appUser);
            model.addAttribute("contacts", contacts);


        });
        return "contact";
    }

    @PostMapping("/register")
    public String registerAppUser(@ModelAttribute AppUser appUser) {

        if (appUserService.createAppUser(appUser) != null) {
            return "registrationSuccessful";
        } else {
            return "registrationFailure";
        }
    }

    @PostMapping("/addContact")
    public String addContact(Principal principal, @RequestParam("contactUsername") String contactUsername) {
        appUserService.addContact(principal.getName(), contactUsername);
        return "redirect:/contact"; // redirect to same page
    }

    @PostMapping("/removeContact")
    public String removeContact(Principal principal, @RequestParam("contactId") Integer contactId) {
        appUserService.removeContact(principal.getName(), contactId);
        return "redirect:/contact"; // redirect to same page
    }

    @PostMapping("/transfer")
    public String transferFunds(Principal principal,
                                @RequestParam("contactId") Integer contactId,
                                @RequestParam("amount") BigDecimal amount,
                                @RequestParam(value = "description", required = false) String description) {

        transactionService.transferFunds(principal.getName(), contactId, amount, description);
        return "redirect:/transfer"; // redirect back to the transfer page
    }

    @PostMapping("/addBankAccount")
    public String addBankAccount(Principal principal,
                                 @RequestParam("lastName") String lastName,
                                 @RequestParam("firstName") String firstName,
                                 @RequestParam("iban") String iban) {

        //check bank account validity
        BankAccount bankAccountToAdd = bankAccountService
                .checkBankAccountValidity(principal
                        .getName(), lastName, firstName, iban);

        //add/update the appusers bank account.
        bankAccountService.updateBankAccount(bankAccountToAdd);
        return "redirect:/profile";
    }

    @PostMapping("/removeBankAccount")
    public String removeBankAccount(@RequestParam("appUserId") Integer appUserId) {
        bankAccountService.removeBankAccount(appUserId);

        return "redirect:/profile";
    }


    @PostMapping("/withdrawFunds")
    public String withdrawFunds(Principal principal, @RequestParam("amount") BigDecimal amount) {
        transactionService.withdrawFunds(principal.getName(), amount);
        return "redirect:/transfer";
    }

    //for test
    //TODO: remove after test
    @PostMapping("/testDepositFunds")
    public String testDepositFunds(Principal principal) {
        transactionService.genarateTestDepostion(principal.getName());
        return "redirect:/transfer";
    }


    @PostMapping("/noAccountForWithdrawal")
    public String indicateAddBankAccount() {
        bankAccountService.noBankAccountForWithdrawal();
        return "redirect:/transfer";
    }

    @GetMapping("update_profile")
    public String goToUpdateProfileInfoPage(Model model, Principal principal) {

        String username = principal.getName();
        Optional<AppUser> currentUser = appUserService.getAppUserByUsername(username);

        currentUser.ifPresent(appUser -> {
            model.addAttribute("currentUser", appUser);
            model.addAttribute("appUser", new AppUser());
        });

        return "update_profile";
    }

    @PostMapping("/updateProfileInfo")
    public String updateProfileInfo(@ModelAttribute AppUser updatedUser, Principal principal, Model model) {
        String username = principal.getName();
        Optional<AppUser> appUserOptional = appUserService.getAppUserByUsername(username);
        AppUser existingAppUser = appUserOptional
                .orElseThrow(() -> new UsernameNotFoundException("User not found Exception"));

        existingAppUser.setFirstName(updatedUser.getFirstName());
        existingAppUser.setLastName(updatedUser.getLastName());
        existingAppUser.setEmail(updatedUser.getEmail());

        appUserService.updateAppUser(existingAppUser);

        return "redirect:/profile";
    }


}
