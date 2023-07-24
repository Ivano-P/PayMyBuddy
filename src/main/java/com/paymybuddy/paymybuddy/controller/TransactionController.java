package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.dto.TransactionForAppUserHistory;
import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.service.AppUserService;
import com.paymybuddy.paymybuddy.service.BankAccountService;
import com.paymybuddy.paymybuddy.service.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@Log4j2
@Controller
public class TransactionController {
    private final TransactionService transactionService;
    private final AppUserService appUserService;
    private final BankAccountService bankAccountService;

    private static final String REDIRECT_TRANSFER = "redirect:/transfer";
    private static final String CURRENT_USER = "currentUser";

    private AppUser getAppUserService(String username){
        log.info("getAppUserService method called");
        Optional<AppUser> currentUserOptional = appUserService.getAppUserByUsername(username);
        return currentUserOptional.orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    @GetMapping("/transfer")
    public String goToTransferPage(Model model, Principal principal, @RequestParam(defaultValue = "0") int page) {
        log.info("goToTransferPage method called for user {} ", principal.getName());
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

        int totalPages = transactions.getTotalPages() - 1;
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);

        return "transfer";
    }

    @PostMapping("/deposit")
    public String depositFunds() {
        log.info("depositFunds method called");
        return "redirect:/iban";
    }

    @PostMapping("/transfer")
    public String transferFunds(Principal principal,
                                @RequestParam("contactId") Integer contactId,
                                @RequestParam("amount") BigDecimal amount,
                                @RequestParam(value = "description", required = false) String description) {
        log.info("transferFunds method called for user {} ", principal.getName());
        transactionService.transferFunds(principal.getName(), contactId, amount, description);
        return REDIRECT_TRANSFER; // redirect back to the transfer page
    }



    @PostMapping("/withdrawFunds")
    public String withdrawFunds(Principal principal, @RequestParam("amount") BigDecimal amount) {
        log.info("withdrawFunds method called for user {} ", principal.getName());
        transactionService.withdrawFunds(principal.getName(), amount);
        return REDIRECT_TRANSFER;
    }

    //TODO: remove in production
    @PostMapping("/testDepositFunds")
    public String testDepositFunds(Principal principal) {
        log.info("testDepositFunds method called for user {}", principal.getName());
        transactionService.genarateTestDeposit(principal.getName());
        return REDIRECT_TRANSFER;
    }


}
