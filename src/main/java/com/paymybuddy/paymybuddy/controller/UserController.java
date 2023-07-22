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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

    private static final String REDIRECT_PROFILE = "redirect:/profile";
    private static final String REDIRECT_TRANSFER = "redirect:/transfer";

    private static final String CURRENT_USER = "currentUser";

    private AppUser getAppUserService(String username){
        Optional<AppUser> currentUserOptional = appUserService.getAppUserByUsername(username);
        return currentUserOptional.orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }


    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("appUser", new AppUser());
        return "register";
    }

    @GetMapping("/home")
    public String goToHomePage(Model model, Principal principal) {
        AppUser currentAppUser = getAppUserService(principal.getName());
        model.addAttribute(CURRENT_USER, currentAppUser);
        this.appUserService.checkIfAllUserInfoPresent(currentAppUser);
        return "home";
    }

    @GetMapping("/transfer")
    public String goToTransferPage(Model model, Principal principal, @RequestParam(defaultValue = "0") int page) {
        AppUser currentAppUser = getAppUserService(principal.getName());
        PageRequest pageRequest = PageRequest.of(page, 5);

        model.addAttribute(CURRENT_USER, currentAppUser);
        this.appUserService.checkIfAllUserInfoPresent(currentAppUser);

        // Fetch the contacts for the current user and add them to the model
        List<AppUser> contacts = this.appUserService.getContactsForUser(currentAppUser);
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

        return "transfer";
    }

    @GetMapping("/iban")
    public String goToIban(Model model, Principal principal) {
        AppUser currentAppUser = getAppUserService(principal.getName());
        model.addAttribute(CURRENT_USER, currentAppUser);

        String iban = appPmbService.getPmbIban();
        model.addAttribute("iban", iban);

        return "iban";
    }

    @PostMapping("/deposit")
    public String depositFunds() {
        return "redirect:/iban";
    }

    @GetMapping("/profile")
    public String goToProfilePage(Model model, Principal principal) {
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

        AppUser currentAppUser = getAppUserService(principal.getName());

        model.addAttribute(CURRENT_USER, currentAppUser);
        appUserService.checkIfAllUserInfoPresent(currentAppUser);

        // Fetch the contacts for the current user and add them to the model
        List<AppUser> contacts = appUserService.getContactsForUser(currentAppUser);
        model.addAttribute("contacts", contacts);

        return "contact";
    }

    @PostMapping("/register")
    public String registerAppUser(@ModelAttribute AppUser appUser, BindingResult bindingResult) {

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
        return REDIRECT_TRANSFER; // redirect back to the transfer page
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

        //add/update the appUsers bank account.
        bankAccountService.updateBankAccount(bankAccountToAdd);
        return REDIRECT_PROFILE;
    }

    @PostMapping("/removeBankAccount")
    public String removeBankAccount(@RequestParam("appUserId") Integer appUserId) {
        bankAccountService.removeBankAccount(appUserId);

        return REDIRECT_PROFILE;
    }


    @PostMapping("/withdrawFunds")
    public String withdrawFunds(Principal principal, @RequestParam("amount") BigDecimal amount) {
        transactionService.withdrawFunds(principal.getName(), amount);
        return REDIRECT_TRANSFER;
    }


    //TODO: remove in production
    @PostMapping("/testDepositFunds")
    public String testDepositFunds(Principal principal) {
        transactionService.genarateTestDepostion(principal.getName());
        return REDIRECT_TRANSFER;
    }


    @PostMapping("/noAccountForWithdrawal")
    public String indicateAddBankAccount() {
        bankAccountService.noBankAccountForWithdrawal();
        return REDIRECT_TRANSFER;
    }

    @GetMapping("update_profile")
    public String goToUpdateProfileInfoPage(Model model, Principal principal) {
        AppUser currentAppUser = getAppUserService(principal.getName());

        model.addAttribute(CURRENT_USER, currentAppUser);
        model.addAttribute("appUser", new AppUser());

        return "update_profile";
    }

    @PostMapping("/updateProfileInfo")
    public String updateProfileInfo(@ModelAttribute AppUser updatedUser, Principal principal) {
        AppUser currentAppUser = getAppUserService(principal.getName());

        currentAppUser.setFirstName(updatedUser.getFirstName());
        currentAppUser.setLastName(updatedUser.getLastName());
        currentAppUser.setEmail(updatedUser.getEmail());

        appUserService.updateAppUser(currentAppUser);

        return REDIRECT_PROFILE;
    }


}
