package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.Wallet;
import com.paymybuddy.paymybuddy.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AppUser createAppUserAndWallet(AppUser appUser) {

        //Encode the password
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));

        // Create new wallet
        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUserEmail(appUser.getEmail());

        //Associate the wallet  with user
        appUser.setWallet(wallet);

        // Set the default role of the user
        appUser.setRole("USER");

        //Set the Wallets user
        wallet.setAppUser(appUser);

        //cascade setting saves the wallet as well
        return userRepository.save(appUser);
    }


    @Transactional(readOnly = true)
    public List<AppUser> getAllAppUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> getAppUserById(int id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> getAppUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public AppUser updateAppUser(AppUser appUser) {
        return userRepository.save(appUser);
    }

    @Transactional
    public void deleteAppUser(int id) {
        userRepository.deleteById(id);
    }



    //creat admin user for test, this  is called on startup
    @Transactional
    public AppUser creatAdminAppUser(){

        //check if first user (ADMIN) is created, if not in db creat it
        Optional<AppUser> userCheck = userRepository.findById(1);
        if(userCheck.isPresent()){
            return null;

        }else {
            AppUser adminAppUser = new AppUser();
            adminAppUser.setLastName("petty");
            adminAppUser.setFirstName("ivano");
            adminAppUser.setEmail("mistertester@testmail.com");
            adminAppUser.setRole("ADMIN");
            adminAppUser.setPassword(passwordEncoder.encode("Testpassword123*"));

            // Create new wallet
            Wallet wallet = new Wallet();
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setUserEmail(adminAppUser.getEmail());

            //Associate the wallet  with user
            adminAppUser.setWallet(wallet);

            //Associate the wallet  with user
            adminAppUser.setWallet(wallet);


            //Set the PorteMonnaie's utilisateur
            wallet.setAppUser(adminAppUser);

            //cascade setting saves the porteMonnaie as well
            return userRepository.save(adminAppUser);
        }


    }

}

