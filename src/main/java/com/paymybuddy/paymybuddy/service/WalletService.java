package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.Wallet;
import com.paymybuddy.paymybuddy.repository.WalletRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Transactional
@Log4j2
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class WalletService {

    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public Optional<Wallet> getWalletById(int walletId){
        return walletRepository.findById(walletId);
    }

    @Transactional(readOnly = true)
    public List<Wallet> getAllWallet() {
        return walletRepository.findAll();
    }

    public void updateWallet(Wallet wallet) {
        walletRepository.save(wallet);
    }


    //set admin user balance for test. add 1 000 000 to admin wallet
    public void setAdminUserWalletBalance(AppUser appUser){
        // Create new wallet
        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.valueOf(10000));
        //Associate the wallet  with user
        appUser.setWallet(wallet);
        //Associate the wallet  with user
        appUser.setWallet(wallet);
        //Set the appUser Wallet
        wallet.setAppUser(appUser);

    }

    public void creatAndLinkWallet(AppUser appUser){
        // Create new wallet
        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.ZERO);
        //Associate the wallet  with user
        appUser.setWallet(wallet);
        //Associate the wallet  with user
        appUser.setWallet(wallet);
        //Set the appUser Wallet
        wallet.setAppUser(appUser);

    }

}
