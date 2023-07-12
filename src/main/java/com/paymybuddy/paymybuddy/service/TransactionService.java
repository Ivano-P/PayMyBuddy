package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.dto.TransactionForAppUserHistory;
import com.paymybuddy.paymybuddy.exceptions.InsufficientFundsException;
import com.paymybuddy.paymybuddy.model.AccountPayMyBuddy;
import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.model.Wallet;
import com.paymybuddy.paymybuddy.repository.AccountPayMyBuddyRepository;
import com.paymybuddy.paymybuddy.repository.AppUserRepository;
import com.paymybuddy.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.paymybuddy.repository.WalletRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Transactional
@Log4j2
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AppUserRepository appUserRepository;
    private final WalletRepository walletRepository;
    private final AccountPayMyBuddyRepository accountPayMyBuddyRepository;
    private final AppPmbService appPmbService;


    public void transferFunds (String appUserUsername, Integer contactId, BigDecimal amount, String description){
        Optional<AppUser> appUserOptional = appUserRepository.findByUsername(appUserUsername);
        Optional<AppUser> contactOptional = appUserRepository.findById(contactId);

        if(appUserOptional.isPresent() && contactOptional.isPresent()) {
            AppUser appUser = appUserOptional.get();
            AppUser contactToTransferTo = contactOptional.get();

            Optional<Wallet> appUserWalletOptional = walletRepository.findById(appUser.getId());
            Optional<Wallet> contactWalletOptional = walletRepository.findById(contactToTransferTo.getId());
            Optional<AccountPayMyBuddy> pmbAccountOptional = accountPayMyBuddyRepository.findById(1);

            if(appUserWalletOptional.isPresent() && contactWalletOptional.isPresent() && pmbAccountOptional
                    .isPresent()){
                Wallet appUserWallet = appUserWalletOptional.get();
                Wallet recepientAppUserWallet = contactWalletOptional.get();
                AccountPayMyBuddy pmbAccount = pmbAccountOptional.get();

                //calculate transaction fee
                BigDecimal transactionFee = amount.multiply(BigDecimal.valueOf(pmbAccount.getTransactionFee()));
                BigDecimal finalTransactionAmount = amount.add(transactionFee);

                //check if sender balance is bigger or equal to amount plus transaction fee
                int comparisonResult = appUserWallet.getBalance().compareTo(finalTransactionAmount);
                if(comparisonResult >= 0){

                    appUserWallet.setBalance(appUserWallet.getBalance().subtract(finalTransactionAmount));
                    recepientAppUserWallet.setBalance(recepientAppUserWallet.getBalance().add(amount));
                    pmbAccount.setBalance(pmbAccount.getBalance().add(transactionFee));

                    walletRepository.save(appUserWallet);
                    walletRepository.save(recepientAppUserWallet);
                    accountPayMyBuddyRepository.save(pmbAccount);

                    if(description.isEmpty()){
                        appPmbService.saveTransaction(appUserWallet.getId(), recepientAppUserWallet.getId(),
                                amount, transactionFee, Transaction.TransactionType.send, Optional.empty());
                    }else{
                        appPmbService.saveTransaction(appUserWallet.getId(), recepientAppUserWallet.getId(),
                                amount, transactionFee, Transaction.TransactionType.send, description
                                        .describeConstable());
                    }


                }else {
                    throw new InsufficientFundsException("Insufficient funds for the transfer.");
                }

            }
        }
    }


    public List<TransactionForAppUserHistory> getTransactionHistory(String username){
        Optional<AppUser> appUserOptional = appUserRepository.findByUsername(username);
        int appUserId;
        if(appUserOptional.isPresent()){
            appUserId = appUserOptional.get().getId();
        }else {
            appUserId = 0;
            log.error("AppUser not found");
            //TODO: figure out what to do it app user not found
        }


        List<Transaction> appUSertransactions = transactionRepository
                .findBySenderIdOrRecepientId(appUserId,appUserId);

        List<TransactionForAppUserHistory> transactionsHistory = new ArrayList<>();

        if(!appUSertransactions.isEmpty()){
            //creat one AppUserTransactionHistory for each transaction in db.
            for(Transaction transaction : appUSertransactions){

                //find contactId in transaction
                int contactId;
                if(appUserId == transaction.getSenderId()){
                    contactId = transaction.getRecepientId();

                    //find the contactAppUSer in db
                    Optional<AppUser> contactAppUserOptionnal = appUserRepository
                            .findById(contactId);

                    //add each TransactionForAppUserHistory to transactionsHistory
                    if (contactAppUserOptionnal.isPresent()){
                        AppUser contact = contactAppUserOptionnal.get();

                        TransactionForAppUserHistory simplifiedTransaction = new TransactionForAppUserHistory(contact
                                .getUsername(), transaction.getDescription(), transaction.getAmount().negate(),
                                Transaction.TransactionType.send);
                        transactionsHistory.add(simplifiedTransaction);
                    }

                }else {
                    contactId = transaction.getSenderId();

                    //find the contactAppUSer in db
                    Optional<AppUser> contactAppUserOptionnal = appUserRepository
                            .findById(contactId);

                    //add each TransactionForAppUserHistory to transactionsHistory
                    if (contactAppUserOptionnal.isPresent()){
                        AppUser contact = contactAppUserOptionnal.get();

                        TransactionForAppUserHistory simplifiedTransaction = new TransactionForAppUserHistory(contact
                                .getUsername(), transaction.getDescription(), transaction.getAmount(),
                                Transaction.TransactionType.receive);
                        transactionsHistory.add(simplifiedTransaction);
                    }

                }

            }

        }

        return transactionsHistory;
    }

}


