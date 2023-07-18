package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.AccountPayMyBuddy;
import com.paymybuddy.paymybuddy.repository.AccountPayMyBuddyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Log4j2
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AppPmbService {
    private final AccountPayMyBuddyRepository accountPayMyBuddyRepository;
    static final int PMB_ACCOUNT_ID = 1;

    public AccountPayMyBuddy getAccountPmb() {
        Optional<AccountPayMyBuddy> accountPayMyBuddyOptional = accountPayMyBuddyRepository.findById(PMB_ACCOUNT_ID);
        return accountPayMyBuddyOptional.orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public boolean checkIfPmbAccountIsPresent() {
        Optional<AccountPayMyBuddy> pmbAccountOptional = accountPayMyBuddyRepository.findById(PMB_ACCOUNT_ID);

        return pmbAccountOptional.isPresent();
    }

    //this is called on launch so i dont want to throw error if no AccountPmb is found
    @Transactional
    public void creatPmbAccount() {
        if (checkIfPmbAccountIsPresent()) {
            AccountPayMyBuddy pmbAccount = new AccountPayMyBuddy();
            pmbAccount.setBalance(BigDecimal.ZERO);
            accountPayMyBuddyRepository.save(pmbAccount);

        } else {
            log.debug("pmb account already created");
        }
    }

    public String getPmbIban() {
        return getAccountPmb().getIban();
    }


}
