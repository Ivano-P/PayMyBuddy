package com.paymybuddy.paymybuddy.implementation;

import com.paymybuddy.paymybuddy.dto.TransactionForAppUserHistory;

import com.paymybuddy.paymybuddy.exceptions.*;

import com.paymybuddy.paymybuddy.model.AccountPayMyBuddy;
import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.model.Wallet;
import com.paymybuddy.paymybuddy.repository.AccountPayMyBuddyRepository;
import com.paymybuddy.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.paymybuddy.service.AppPmbService;
import com.paymybuddy.paymybuddy.service.AppUserService;
import com.paymybuddy.paymybuddy.service.TransactionService;
import com.paymybuddy.paymybuddy.service.WalletService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final AccountPayMyBuddyRepository accountPayMyBuddyRepository;
    private final AppUserService appUserService;
    private final AppPmbService appPmbService;

private static final String CURRENT_USER_NOT_FOUND = "current user not found";

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
        if(amount.compareTo(BigDecimal.ZERO) <=0 || amount.compareTo(BigDecimal.valueOf(10000)) > 0){
            throw new InvalidAmountException();
        }
        Optional<AppUser> appUserOptional = appUserService.getAppUserByUsername(appUserUsername);
        Optional<AppUser> contactOptional = appUserService.getAppUserById(contactId);

        AppUser contactAppUser = contactOptional.orElseThrow(NoContactSelectedException::new);
        AppUser appUser = appUserOptional.orElseThrow(() -> new UsernameNotFoundException(CURRENT_USER_NOT_FOUND));


        Optional<Wallet> appUserWalletOptional = walletService.getWalletById(appUser.getId());
        Optional<Wallet> contactWalletOptional = walletService.getWalletById(contactAppUser.getId());

        Wallet appUserWallet = appUserWalletOptional
                .orElseThrow(() -> new WalletNotFoundException("User wallet nor found"));
        Wallet recepientAppUserWallet = contactWalletOptional
                .orElseThrow(() -> new WalletNotFoundException("Contact wallet nor found"));

        AccountPayMyBuddy pmbAccount = appPmbService.getAccountPmb();

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
                        amount, transactionFee, Transaction.TransactionType.SEND, Optional.empty());
            }else{
                saveTransaction(appUserWallet.getId(), recepientAppUserWallet.getId(),
                        amount, transactionFee, Transaction.TransactionType.SEND, description
                                .describeConstable());
            }

        }else {
            throw new InsufficientFundsException("Insufficient funds for the transfer.");
        }
    }


    // get the paged transaction history of a specific user
    public Page<TransactionForAppUserHistory> getTransactionHistory(String username, Pageable pageable){
        int appUserId = appUserService.getAppUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("AppUser not found"))
                .getId();

        Page<Transaction> appUserTransactions = transactionRepository
                .findBySenderIdOrRecepientIdOrderByIdDesc(appUserId, appUserId, pageable);

       /* Map each Transaction object in the Page into a TransactionForAppUserHistory object
          The resulting Page contains the same paging information as the original, but with the mapped content
       */
        return appUserTransactions.map(transaction -> convertToTransactionHistory(transaction, appUserId));
    }

    //convert a Transaction object to a TransactionForAppUserHistory object
    private TransactionForAppUserHistory convertToTransactionHistory(Transaction transaction, int appUserId) {

        TransactionForAppUserHistory simplifiedTransaction;

        // If the transaction type is send or receive,
        // then handle it with handleSendReceiveTransactions
        if(transaction.getTransactionType() == Transaction.TransactionType.SEND
                || transaction.getTransactionType() == Transaction.TransactionType.RECEIVE) {
            simplifiedTransaction = handleSendReceiveTransactions(transaction, appUserId);
        } else {
            // Otherwise, handle it with handleNonDepositWithdrawalTransactions
            simplifiedTransaction = handleNonDepositWithdrawalTransactions(transaction);
        }

        // Return the converted transaction
        return simplifiedTransaction;
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
                         Transaction.TransactionType.SEND);
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
                        Transaction.TransactionType.RECEIVE);
            }
        }
        return null;
    }

    private TransactionForAppUserHistory handleNonDepositWithdrawalTransactions(Transaction transaction) {
        if (transaction.getTransactionType() == Transaction
                .TransactionType.WITHDRAWAL){

            return new TransactionForAppUserHistory(null,
                    transaction.getDescription(), transaction.getAmount().negate(),
                    Transaction.TransactionType.WITHDRAWAL);


        }else if (transaction.getTransactionType() == Transaction.TransactionType.DEPOSIT){
            return new TransactionForAppUserHistory(null,
                    transaction.getDescription(), transaction.getAmount(),
                    Transaction.TransactionType.DEPOSIT);

        }
        return null;
    }

    public void withdrawFunds(String username, BigDecimal amount){
        //get id
        AppUser appUser = appUserService.getAppUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(CURRENT_USER_NOT_FOUND));


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
                Transaction.TransactionType.WITHDRAWAL, Optional.of("Bank account withdrawal"));

    }

    //TODO: remove in production
    public void genarateTestDepostion(String username){
        //get id
        AppUser appUser = appUserService.getAppUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(CURRENT_USER_NOT_FOUND));

        Wallet appUserWallet = null;
        Optional<Wallet> walletOptional =  walletService.getWalletById(appUser.getId());
        appUserWallet = walletOptional.orElseThrow(() -> new WalletNotFoundException("wallet not found exception"));

        BigDecimal amount = BigDecimal.valueOf(1000);
        appUserWallet.setBalance(appUserWallet.getBalance().add(amount));
        walletService.updateWallet(appUserWallet);
        saveTransaction(appUserWallet.getId(), appUser.getId(), amount, BigDecimal.ZERO,
                Transaction.TransactionType.DEPOSIT, Optional.of("Bank account deposit"));

    }

}


