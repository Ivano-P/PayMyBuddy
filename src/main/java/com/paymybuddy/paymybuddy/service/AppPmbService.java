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
    final int PMBACCOUNTID = 1;

    public Optional<AccountPayMyBuddy> getPmbAccountOptional(){
        return accountPayMyBuddyRepository.findById(PMBACCOUNTID);
    }

    @Transactional
    public AccountPayMyBuddy creatPmbAccount(){
        getPmbAccountOptional();
        if(getPmbAccountOptional().isPresent()){
            return null;
        }else {
            AccountPayMyBuddy pmbAccount = new AccountPayMyBuddy();
            pmbAccount.setBalance(BigDecimal.ZERO);
            return accountPayMyBuddyRepository.save(pmbAccount);
        }
    }


    public String getPmbIban(){
        String pmbIban = null;

        if(getPmbAccountOptional().isPresent()){
            pmbIban = getPmbAccountOptional().get().getIban();
        }else {
            log.error("pmb account is not persisted in db or does not contain iban");

        }

        return pmbIban;
    }


}
