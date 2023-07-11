package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.AccountPayMyBuddy;
import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.repository.AccountPayMyBuddyRepository;
import com.paymybuddy.paymybuddy.repository.TransactionRepository;
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
    private final TransactionRepository transactionRepository;

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

    public Transaction persistTransaction(int senderId, int recepientId,
                                          BigDecimal amout, BigDecimal transactionFee,
                                          Transaction.TransactionType transactionType,
                                          Optional<String> description){
        Transaction transaction = new Transaction();
        transaction.setSenderId(senderId);
        transaction.setRecepientId(recepientId);
        transaction.setAmount(amout);
        transaction.setTransactionFee(transactionFee);
        transaction.setTransactionType(transactionType);
        description.ifPresent(transaction::setDescription);

        return transactionRepository.save(transaction);
    }



}
