package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.repository.AppUserContactRepository;
import com.paymybuddy.paymybuddy.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private AppUserContactRepository appUserContactRepository;
    @Mock
    private WalletService walletService;
    @Mock
    private PasswordEncoder passwordEncoder;


    private AppUserService appUserService;

    @BeforeEach
    public void setup() {
        appUserService = new AppUserService(appUserRepository, appUserContactRepository
                                                        , walletService, passwordEncoder);
    }

    @Test
    void testGetAppUserById_UserExists() {
        AppUser mockUser = new AppUser();
        mockUser.setId(1);
        when(appUserRepository.findById(1)).thenReturn(Optional.of(mockUser));

        Optional<AppUser> result = appUserService.getAppUserById(1);

        assertThat(result).contains(mockUser);
    }

    @Test
    void testGetAppUserById_UserDoesNotExist() {
        when(appUserRepository.findById(1)).thenReturn(Optional.empty());

        Optional<AppUser> result = appUserService.getAppUserById(1);

        assertThat(result).isEmpty();
    }

    @Test
    void testGetAppUserByEmail_UserExists() {
        AppUser mockUser = new AppUser();
        mockUser.setEmail("test@test.com");
        when(appUserRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));

        Optional<AppUser> result = appUserService.getAppUserByEmail("test@test.com");

        assertThat(result).contains(mockUser);
    }

    @Test
    void testGetAppUserByEmail_UserDoesNotExist() {
        when(appUserRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        Optional<AppUser> result = appUserService.getAppUserByEmail("test@test.com");

        assertThat(result).isEmpty();
    }


    @Test
    void testCreatMainAdminAppUser_AdminDoesNotExist() {
        // ARRANGE
        when(appUserRepository.findById(1)).thenReturn(Optional.empty());

        // ACT
        appUserService.creatMainAdminAppUser();

        ArgumentCaptor<AppUser> appUserArgumentCaptor = ArgumentCaptor.forClass(AppUser.class);

        // ASSERT
        verify(appUserRepository, times(1)).save(appUserArgumentCaptor.capture());
        verify(walletService, times(1)).setAdminUserWalletBalance(appUserArgumentCaptor.capture());

        AppUser savedAppUser = appUserArgumentCaptor.getValue();
        AppUser walletAppUser = appUserArgumentCaptor.getValue();

        assertThat(savedAppUser.getUsername()).isEqualTo("mainadmin");
        assertThat(savedAppUser.getEmail()).isEqualTo("mistertester@testmail.com");

    }

    @Test
    void testCreateAppUser() {
        // ARRANGE
        AppUser appUser = new AppUser();
        appUser.setPassword("rawPassword");
        appUser.setUsername("user");

        AppUser savedAppUser = new AppUser();
        savedAppUser.setUsername("user");
        savedAppUser.setPassword("encodedPassword");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(savedAppUser);
        doNothing().when(walletService).creatAndLinkWallet(any(AppUser.class));

        // ACT
        AppUser result = appUserService.createAppUser(appUser);

        // ASSERT
        verify(passwordEncoder, times(1)).encode("rawPassword");
        verify(walletService, times(1)).creatAndLinkWallet(appUser);

        ArgumentCaptor<AppUser> appUserArgumentCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository, times(1)).save(appUserArgumentCaptor.capture());

        AppUser capturedAppUser = appUserArgumentCaptor.getValue();
        assertThat(capturedAppUser.getPassword()).isEqualTo("encodedPassword");

        assertThat(result).isEqualTo(savedAppUser);
    }

    @Test
    void testAddContact() {
        fail();
    }
}

