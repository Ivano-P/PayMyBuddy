package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.AccountPayMyBuddy;
import com.paymybuddy.paymybuddy.repository.AccountPayMyBuddyRepository;
import com.paymybuddy.paymybuddy.repository.TransactionRepository;
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

    @Transactional
    public AccountPayMyBuddy creatPmbAccount(){
        Optional<AccountPayMyBuddy> pmAccountOptional = accountPayMyBuddyRepository.findById(1);
        if(pmAccountOptional.isPresent()){
            return null;
        }else {
            AccountPayMyBuddy pmbAccount = new AccountPayMyBuddy();
            pmbAccount.setBalance(BigDecimal.ZERO);
            return accountPayMyBuddyRepository.save(pmbAccount);
        }
    }


    public String getPmbIban(){
        Optional<AccountPayMyBuddy> accountPmbOptional = accountPayMyBuddyRepository.findById(1);
        String pmbIban = null;

        if(accountPmbOptional.isPresent()){
            pmbIban = accountPmbOptional.get().getIban();
        }else {
            log.error("pmb account is not persisted in db or does not contain iban");

        }

        return pmbIban;
    }


}
