package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.dto.TransactionForAppUserHistory;

import com.paymybuddy.paymybuddy.exceptions.InsufficientFundsException;
import com.paymybuddy.paymybuddy.exceptions.NoContactSelectedException;
import com.paymybuddy.paymybuddy.exceptions.PmbAccountNotFound;
import com.paymybuddy.paymybuddy.exceptions.WalletNotFoundException;

import com.paymybuddy.paymybuddy.model.AccountPayMyBuddy;
import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.model.Wallet;
import com.paymybuddy.paymybuddy.repository.AccountPayMyBuddyRepository;
import com.paymybuddy.paymybuddy.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.*;

@Transactional
@Log4j2
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final AccountPayMyBuddyRepository accountPayMyBuddyRepository;
    private final AppUserService appUserService;

    public void saveTransaction(int senderId, int recepientId,
                                BigDecimal amout, BigDecimal transactionFee,
                                Transaction.TransactionType transactionType,
                                Optional<String> description){
        Transaction transaction = new Transaction();
        transaction.setSenderId(senderId);
        transaction.setRecepientId(recepientId);
        transaction.setAmount(amout);
        transaction.setTimeStamp(LocalDateTime.now());
        transaction.setTransactionFee(transactionFee);
        transaction.setTransactionType(transactionType);
        description.ifPresent(transaction::setDescription);

        transactionRepository.save(transaction);
    }


    public void transferFunds (String appUserUsername, Integer contactId, BigDecimal amount, String description){
        Optional<AppUser> appUserOptional = appUserService.getAppUserByUsername(appUserUsername);
        Optional<AppUser> contactOptional = appUserService.getAppUserById(contactId);

        AppUser contactAppUser = contactOptional.orElseThrow(NoContactSelectedException::new);
        AppUser appUser = appUserOptional.orElseThrow(() -> new UsernameNotFoundException("User not found"));


        Optional<Wallet> appUserWalletOptional = walletService.getWalletById(appUser.getId());
        Optional<Wallet> contactWalletOptional = walletService.getWalletById(contactAppUser.getId());
        Optional<AccountPayMyBuddy> pmbAccountOptional = accountPayMyBuddyRepository.findById(1);


        Wallet appUserWallet = appUserWalletOptional
                .orElseThrow(() -> new WalletNotFoundException("User wallet nor found"));
        Wallet recepientAppUserWallet = contactWalletOptional
                .orElseThrow(() -> new WalletNotFoundException("Contact wallet nor found"));

        AccountPayMyBuddy pmbAccount = pmbAccountOptional.orElseThrow(PmbAccountNotFound::new);

        //calculate transaction fee
        BigDecimal transactionFee = amount.multiply(BigDecimal.valueOf(pmbAccount.getTransactionFee()));
        BigDecimal finalTransactionAmount = amount.add(transactionFee);

        //check if sender balance is bigger or equal to amount plus transaction fee
        int comparisonResult = appUserWallet.getBalance().compareTo(finalTransactionAmount);
        if(comparisonResult >= 0){

            appUserWallet.setBalance(appUserWallet.getBalance().subtract(finalTransactionAmount));
            recepientAppUserWallet.setBalance(recepientAppUserWallet.getBalance().add(amount));
            pmbAccount.setBalance(pmbAccount.getBalance().add(transactionFee));

            walletService.updateWallet(appUserWallet);
            walletService.updateWallet(recepientAppUserWallet);
            accountPayMyBuddyRepository.save(pmbAccount);

            if(description.isEmpty()){
                saveTransaction(appUserWallet.getId(), recepientAppUserWallet.getId(),
                        amount, transactionFee, Transaction.TransactionType.send, Optional.empty());
            }else{
                saveTransaction(appUserWallet.getId(), recepientAppUserWallet.getId(),
                        amount, transactionFee, Transaction.TransactionType.send, description
                                .describeConstable());
            }

        }else {
            throw new InsufficientFundsException("Insufficient funds for the transfer.");
        }
    }


    public List<TransactionForAppUserHistory> getTransactionHistory(String username){
        int appUserId = appUserService.getAppUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("AppUser not found"))
                .getId();

        List<Transaction> appUserTransactions = transactionRepository
                .findBySenderIdOrRecepientIdOrderByIdDesc(appUserId, appUserId);

        return convertToTransactionHistory(appUserTransactions, appUserId);
    }


    private List<TransactionForAppUserHistory> convertToTransactionHistory(List<Transaction> transactions, int appUserId) {
        List<TransactionForAppUserHistory> transactionsHistory = new ArrayList<>();

        for(Transaction transaction : transactions){
            TransactionForAppUserHistory simplifiedTransaction;

            if(transaction.getTransactionType() == Transaction.TransactionType.send
                    || transaction.getTransactionType() == Transaction.TransactionType.receive) {
                simplifiedTransaction = handleSendReceiveTransactions(transaction, appUserId);
            } else {
                simplifiedTransaction = handleNonDepositWithdrawalTransactions(transaction);
            }

            if (simplifiedTransaction != null) {
                transactionsHistory.add(simplifiedTransaction);
            }
        }
        return transactionsHistory;
    }

    private TransactionForAppUserHistory handleSendReceiveTransactions(Transaction transaction, int appUserId) {
        //find contactId in transaction
        int contactId;
        if(appUserId == transaction.getSenderId()){
            contactId = transaction.getRecepientId();

            //find the contactAppUSer in db
            Optional<AppUser> contactAppUserOptional = appUserService
                    .getAppUserById(contactId);

            //add each TransactionForAppUserHistory to transactionsHistory
            if (contactAppUserOptional.isPresent()){
                AppUser contact = contactAppUserOptional.get();

                return new TransactionForAppUserHistory(contact
                         .getUsername(), transaction.getDescription(), transaction.getAmount().negate(),
                         Transaction.TransactionType.send);
            }

        }else {
            contactId = transaction.getSenderId();

            //find the contactAppUSer in db
            Optional<AppUser> contactAppUserOptionnal = appUserService
                    .getAppUserById(contactId);

            //add each TransactionForAppUserHistory to transactionsHistory
            if (contactAppUserOptionnal.isPresent()){
                AppUser contact = contactAppUserOptionnal.get();

                return new TransactionForAppUserHistory(contact
                        .getUsername(), transaction.getDescription(), transaction.getAmount(),
                        Transaction.TransactionType.receive);
            }
        }
        return null;
    }

    private TransactionForAppUserHistory handleNonDepositWithdrawalTransactions(Transaction transaction) {
        if (transaction.getTransactionType() == Transaction
                .TransactionType.withdrawal){

            return new TransactionForAppUserHistory(null,
                    transaction.getDescription(), transaction.getAmount().negate(),
                    Transaction.TransactionType.withdrawal);


        }else if (transaction.getTransactionType() == Transaction.TransactionType.deposit ){
            return new TransactionForAppUserHistory(null,
                    transaction.getDescription(), transaction.getAmount(),
                    Transaction.TransactionType.deposit);

        }
        return null;
    }

    public void withdrawFunds(String username, BigDecimal amount){
        //get id
        AppUser appUser = appUserService.getAppUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));


        Wallet appUserWallet = null;
        Optional<Wallet> walletOptional =  walletService.getWalletById(appUser.getId());
        if(walletOptional.isPresent()){
            appUserWallet = walletOptional.get();
        }
        assert appUserWallet != null;

        //check if user has the funds for withdrawal
        //check if sender balance is bigger or equal to amount plus transaction fee
        int comparisonResult = appUserWallet.getBalance().compareTo(amount);
        if(comparisonResult < 0){
            throw new InsufficientFundsException("Insufficient funds for withdrawal.");
        }

        appUserWallet.setBalance(appUserWallet.getBalance().subtract(amount));
        walletService.updateWallet(appUserWallet);
        saveTransaction(appUserWallet.getId(), appUser.getId(), amount, BigDecimal.ZERO,
                Transaction.TransactionType.withdrawal, Optional.of("Bank account withdrawal"));

    }

    //TODO: remove in production
    public void genarateTestDepostion(String username){
        //get id
        AppUser appUser = appUserService.getAppUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Wallet appUserWallet = null;
        Optional<Wallet> walletOptional =  walletService.getWalletById(appUser.getId());
        appUserWallet = walletOptional.orElseThrow(() -> new WalletNotFoundException("wallet not found exception"));

        BigDecimal amount = BigDecimal.valueOf(1000);
        appUserWallet.setBalance(appUserWallet.getBalance().add(amount));
        walletService.updateWallet(appUserWallet);
        saveTransaction(appUserWallet.getId(), appUser.getId(), amount, BigDecimal.ZERO,
                Transaction.TransactionType.deposit, Optional.of("Bank account deposit"));

    }

}


