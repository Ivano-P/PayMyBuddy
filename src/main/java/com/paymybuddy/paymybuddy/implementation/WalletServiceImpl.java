package com.paymybuddy.paymybuddy.implementation;

import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.Wallet;
import com.paymybuddy.paymybuddy.repository.WalletRepository;
import com.paymybuddy.paymybuddy.service.WalletService;
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
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public Optional<Wallet> getWalletById(int walletId){
        log.info("getWalletById method called with: {}", walletId);
        return walletRepository.findById(walletId);
    }

    @Transactional(readOnly = true)
    public List<Wallet> getAllWallet() {
        log.info("getWalletById method called");
        return walletRepository.findAll();
    }

    public void updateWallet(Wallet wallet) {
        log.info("updateWallet method called with: {}", wallet);
        walletRepository.save(wallet);
    }


    //set admin user balance for test. add 1 000 000 to admin wallet
    public void setAdminUserWalletBalance(AppUser appUser){
        log.info("setAdminUserWalletBalance method called with: {}", appUser);
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
        log.info("creatAndLinkWallet method called with: {}",appUser);
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
