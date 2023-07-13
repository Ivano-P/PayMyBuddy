package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.dto.TransactionForAppUserHistory;
import com.paymybuddy.paymybuddy.exceptions.InsufficientFundsException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
        transaction.setTransactionFee(transactionFee);
        transaction.setTransactionType(transactionType);
        description.ifPresent(transaction::setDescription);

        transactionRepository.save(transaction);
    }

    public void transferFunds (String appUserUsername, Integer contactId, BigDecimal amount, String description){
        Optional<AppUser> appUserOptional = appUserService.getAppUserByUsername(appUserUsername);
        Optional<AppUser> contactOptional = appUserService.getAppUserById(contactId);

        if(appUserOptional.isPresent() && contactOptional.isPresent()) {
            AppUser appUser = appUserOptional.get();
            AppUser contactToTransferTo = contactOptional.get();

            Optional<Wallet> appUserWalletOptional = walletService.getWalletById(appUser.getId());
            Optional<Wallet> contactWalletOptional = walletService.getWalletById(contactToTransferTo.getId());
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
        }
    }


    public List<TransactionForAppUserHistory> getTransactionHistory(String username){
        int appUserId = appUserService.getAppUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("AppUser not found"))
                .getId();


        List<Transaction> appUSertransactions = transactionRepository
                .findBySenderIdOrRecepientId(appUserId,appUserId);

        List<TransactionForAppUserHistory> transactionsHistory = new ArrayList<>();

        if(!appUSertransactions.isEmpty()){
            //creat one AppUserTransactionHistory for each transaction in db.
            for(Transaction transaction : appUSertransactions){

                if(transaction.getTransactionType() == Transaction
                        .TransactionType.send || transaction.getTransactionType() == Transaction
                        .TransactionType.receive){

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

                            TransactionForAppUserHistory simplifiedTransaction = new TransactionForAppUserHistory(contact
                                    .getUsername(), transaction.getDescription(), transaction.getAmount().negate(),
                                    Transaction.TransactionType.send);
                            transactionsHistory.add(simplifiedTransaction);
                        }

                    }else {
                        contactId = transaction.getSenderId();

                        //find the contactAppUSer in db
                        Optional<AppUser> contactAppUserOptionnal = appUserService
                                .getAppUserById(contactId);

                        //add each TransactionForAppUserHistory to transactionsHistory
                        if (contactAppUserOptionnal.isPresent()){
                            AppUser contact = contactAppUserOptionnal.get();

                            TransactionForAppUserHistory simplifiedTransaction = new TransactionForAppUserHistory(contact
                                    .getUsername(), transaction.getDescription(), transaction.getAmount(),
                                    Transaction.TransactionType.receive);
                            transactionsHistory.add(simplifiedTransaction);
                        }

                    }

                } else if (transaction.getTransactionType() == Transaction
                        .TransactionType.withdrawal){

                    TransactionForAppUserHistory simplifiedTransaction = new TransactionForAppUserHistory(null,
                            transaction.getDescription(), transaction.getAmount().negate(),
                            Transaction.TransactionType.withdrawal);
                    transactionsHistory.add(simplifiedTransaction);

                }else if (transaction.getTransactionType() == Transaction.TransactionType.deposit ){
                    TransactionForAppUserHistory simplifiedTransaction = new TransactionForAppUserHistory(null,
                            transaction.getDescription(), transaction.getAmount(),
                            Transaction.TransactionType.deposit);
                    transactionsHistory.add(simplifiedTransaction);

                }

            }

        }

        return transactionsHistory;
    }

    public void withdrawFunds(String username, BigDecimal amount){
        //get id
        Optional<AppUser> appUserOptional = appUserService.getAppUserByUsername(username);
        AppUser appUser = null;
        if(appUserOptional.isPresent()){
            appUser = appUserOptional.get();
        }else {
            throw new NoSuchElementException("User not found");
        }

        Wallet appUserWallet = null;
        Optional<Wallet> walletOptional =  walletService.getWalletById(appUser.getId());
        if(walletOptional.isPresent()){
            appUserWallet = walletOptional.get();
        }
        assert appUserWallet != null;
        appUserWallet.setBalance(appUserWallet.getBalance().subtract(amount));
        walletService.updateWallet(appUserWallet);
        saveTransaction(appUserWallet.getId(), appUser.getId(), amount, BigDecimal.ZERO,
                Transaction.TransactionType.withdrawal, Optional.of("Bank account withdrawal"));

    }

    //for test
    public void genarateTestDepostion(String username){
        //get id
        Optional<AppUser> appUserOptional = appUserService.getAppUserByUsername(username);
        AppUser appUser = null;
        if(appUserOptional.isPresent()){
            appUser = appUserOptional.get();
        }else {
            throw new NoSuchElementException("User not found");
        }

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


