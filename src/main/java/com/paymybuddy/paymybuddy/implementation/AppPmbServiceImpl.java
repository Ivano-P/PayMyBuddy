package com.paymybuddy.paymybuddy.implementation;

import com.paymybuddy.paymybuddy.model.AccountPayMyBuddy;
import com.paymybuddy.paymybuddy.repository.AccountPayMyBuddyRepository;
import com.paymybuddy.paymybuddy.service.AppPmbService;
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
public class AppPmbServiceImpl implements AppPmbService{

    public static final Integer PMB_ACCOUNT_ID = 1;
    private final AccountPayMyBuddyRepository accountPayMyBuddyRepository;

    public AccountPayMyBuddy getAccountPmb() {
        log.info("getAccountPmb method called");
        Optional<AccountPayMyBuddy> accountPayMyBuddyOptional = accountPayMyBuddyRepository.findById(PMB_ACCOUNT_ID);
        return accountPayMyBuddyOptional.orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public boolean checkIfPmbAccountIsPresent() {
        log.info("checkIfPmbAccountIsPresent method called");
        Optional<AccountPayMyBuddy> pmbAccountOptional = accountPayMyBuddyRepository.findById(PMB_ACCOUNT_ID);

        return pmbAccountOptional.isPresent();
    }

    //this is called on launch. I don't want to throw error if no AccountPmb is found, I just want it to be created
    @Transactional
    public void creatPmbAccount() {
        log.info("creatPmbAccount method called");
        if (!checkIfPmbAccountIsPresent()) {
            AccountPayMyBuddy pmbAccount = new AccountPayMyBuddy();
            pmbAccount.setBalance(BigDecimal.ZERO);
            accountPayMyBuddyRepository.save(pmbAccount);

        } else {
            log.debug("pmb account already created");
        }
    }

    public String getPmbIban() {
        log.info("getPmbIban method called");
        return getAccountPmb().getIban();
    }


}
