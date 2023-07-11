package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.repository.AppUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

        UserDetails userDetails;
        if (appUser.getPassword() == null) {  // For OAuth2 user
            userDetails = User
                    .withUsername(appUser.getUsername())
                    //.password("{noop}")  // No password
                    .roles(appUser.getRole().name())
                    .build();
        } else {  // For normal user
            userDetails = User
                    .withUsername(appUser.getUsername())
                    .password(appUser.getPassword())
                    .roles(appUser.getRole().name())
                    .build();
        }
        return userDetails;
    }
}
