package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.exceptions.ContactNotFoundException;
import com.paymybuddy.paymybuddy.exceptions.MissingUserInfoException;
import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.AppUserContact;
import com.paymybuddy.paymybuddy.repository.AppUserContactRepository;
import com.paymybuddy.paymybuddy.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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
    void testAddContact_contactExist() {
        //ASSERT
        String userUsername = "username";
        String contactUsername = "contact";

        AppUser user = new AppUser();
        user.setId(1);
        user.setUsername(userUsername);

        AppUser contact = new AppUser();
        contact.setId(2);
        contact.setUsername(contactUsername);


        when(appUserRepository.findByUsername(userUsername)).thenReturn(Optional.of(user));
        when(appUserRepository.findByUsername(contactUsername)).thenReturn(Optional.of(contact));

        //ACT
        appUserService.addContact(userUsername, contactUsername);


        // ASSERT
        ArgumentCaptor<AppUserContact> appUserContactArgumentCaptor = ArgumentCaptor.forClass(AppUserContact.class);
        verify(appUserContactRepository, times(1)).save(appUserContactArgumentCaptor.capture());

        AppUserContact capturedAppUserContact = appUserContactArgumentCaptor.getValue();
        assertThat(capturedAppUserContact.getAppUser()).isEqualTo(user);
        assertThat(capturedAppUserContact.getContact()).isEqualTo(contact);
    }

    @Test
    void testGetContactsForUser() {
        // Arrange
        AppUser user = new AppUser();
        user.setId(1);

        AppUser contact1 = new AppUser();
        contact1.setId(2);
        AppUser contact2 = new AppUser();
        contact2.setId(3);

        AppUserContact userContact1 = new AppUserContact();
        userContact1.setAppUser(user);
        userContact1.setContact(contact1);

        AppUserContact userContact2 = new AppUserContact();
        userContact2.setAppUser(user);
        userContact2.setContact(contact2);

        List<AppUserContact> userContacts = List.of(userContact1, userContact2);
        when(appUserContactRepository.findByAppUser(user)).thenReturn(userContacts);

        // Act
        List<AppUser> contacts = appUserService.getContactsForUser(user);

        // Assert
        assertEquals(2, contacts.size());
        assertTrue(contacts.contains(contact1));
        assertTrue(contacts.contains(contact2));
    }


    @Test
    void testAddContact_contactDoesntExist() {
        //ASSERT
        String userUsername = "username";
        String nonExistantUser = "harry";

        AppUser user = new AppUser();
        user.setId(1);
        user.setUsername(userUsername);

        when(appUserRepository.findByUsername(userUsername)).thenReturn(Optional.of(user));
        when(appUserRepository.findByUsername(nonExistantUser)).thenReturn(Optional.empty());


        // Act & Assert
        Exception exception =  assertThrows(ContactNotFoundException.class, () -> appUserService
                .addContact(userUsername, nonExistantUser));
    }

    @Test
    void testRemoveContact_BothUserAndContactExist_ContactRemoved() {
        // Arrange
        String appUserUsername = "username";
        Integer contactId = 2;

        AppUser appUser = new AppUser();
        appUser.setId(1);
        appUser.setUsername(appUserUsername);

        AppUser contactToRemove = new AppUser();
        contactToRemove.setId(contactId);

        AppUserContact.AppUserContactId id = new AppUserContact.AppUserContactId();
        id.setAppUserId(appUser.getId());
        id.setContactId(contactToRemove.getId());

        when(appUserRepository.findByUsername(appUserUsername)).thenReturn(Optional.of(appUser));
        when(appUserRepository.findById(contactId)).thenReturn(Optional.of(contactToRemove));
        when(appUserContactRepository.existsById(id)).thenReturn(true);

        // Act
        appUserService.removeContact(appUserUsername, contactId);

        // Assert
        verify(appUserContactRepository, times(1)).deleteById(id);
    }

    @Test
    void testCheckIfAllUserInfoPresent_AllInfoPresent_NoExceptionThrown() {
        // Arrange
        AppUser appUser = new AppUser();
        appUser.setFirstName("John");
        appUser.setLastName("Doe");
        appUser.setEmail("johndoe@example.com");

        // Act and Assert
        assertDoesNotThrow(() -> appUserService.checkIfAllUserInfoPresent(appUser));
    }

    @Test
    void testCheckIfAllUserInfoPresent_MissingInfo_ExceptionThrown() {
        // Arrange
        AppUser appUser = new AppUser();
        appUser.setFirstName("John");
        appUser.setLastName(null);
        appUser.setEmail("johndoe@example.com");

        // Act and Assert
        assertThrows(MissingUserInfoException.class, () -> appUserService.checkIfAllUserInfoPresent(appUser));
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



}

