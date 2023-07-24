package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.implementation.CustomUserDetailsService;
import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceImplTest {

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Mock
    private AppUserRepository appUserRepository;


    @Test
    void loadUserByUsername() {
        // Arrange
        AppUser appUser = new AppUser();
        appUser.setUsername("username");
        appUser.setPassword("password");
        appUser.setRole(AppUser.Role.ADMIN); // Assume Role is an Enum
        when(appUserRepository.findByUsernameOrEmail(anyString(), anyString()))
                .thenReturn(Optional.of(appUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("username");

        // Assert
        assertNotNull(userDetails);
        assertEquals("username", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_notFound() {
        // Arrange
        when(appUserRepository.findByUsernameOrEmail(anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("unknown");
        });
    }
}

