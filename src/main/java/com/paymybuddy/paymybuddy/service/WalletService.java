package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.Wallet;

import java.util.List;
import java.util.Optional;

public interface WalletService {
    Optional<Wallet> getWalletById(int walletId);
    List<Wallet> getAllWallet();
    void updateWallet(Wallet wallet);
    void setAdminUserWalletBalance(AppUser appUser);
    void creatAndLinkWallet(AppUser appUser);
}
