package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.AccountPayMyBuddy;
import com.paymybuddy.paymybuddy.repository.AccountPayMyBuddyRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

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



}
