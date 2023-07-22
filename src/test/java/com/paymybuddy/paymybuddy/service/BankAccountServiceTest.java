package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.exceptions.AccountMustBeToUsersNameException;
import com.paymybuddy.paymybuddy.exceptions.InvalidIbanException;
import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.BankAccount;
import com.paymybuddy.paymybuddy.repository.BankAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.FactoryBasedNavigableListAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @InjectMocks
    private BankAccountService bankAccountService;
    @Mock
    private BankAccountRepository bankAccountRepository;
    @Mock
    private AppUserService appUserService;


    @AfterEach
    public void tearDown() {
        // Reset mocks after each test
        reset(bankAccountRepository, appUserService);
    }

    @Test
    void testRemoveBankAccount() {
        // Arrange
        int appUserId = 1;

        // Act
        bankAccountService.removeBankAccount(appUserId);

        // Assert
        verify(bankAccountRepository, times(1)).deleteById(appUserId);
    }

    @Test
    void testUpdateBankAccount_BankAccountDoesntExist() {
        // Arrange
        BankAccount bankAccount = new BankAccount();
        bankAccount.setId(1);

        when(bankAccountRepository.findById(bankAccount.getId())).thenReturn(Optional.empty());

        // Act
        bankAccountService.updateBankAccount(bankAccount);

        // Assert
        verify(bankAccountRepository, times(1)).save(bankAccount);
        verify(bankAccountRepository, times(0)).deleteById(bankAccount.getId());
    }

    @Test
    void testUpdateBankAccount_AccountExists() {
        // Arrange
        BankAccount bankAccount = new BankAccount();
        bankAccount.setId(1);

        when(bankAccountRepository.findById(bankAccount.getId())).thenReturn(Optional.of(bankAccount));

        // Act
        bankAccountService.updateBankAccount(bankAccount);

        // Assert
        verify(bankAccountRepository).deleteById(bankAccount.getId());
        verify(bankAccountRepository).save(bankAccount);
    }


    @Test
    void testCheckBankAccountValidity_ValidBankAccount() {
        // Arrange
        String username = "username";
        String lastName = "lastName";
        String firstName = "firstName";
        String iban = "GB82WEST12345698765432";

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setLastName(lastName);
        appUser.setFirstName(firstName);

        when(appUserService.getAppUserByUsername(username)).thenReturn(Optional.of(appUser));

        // Act
        BankAccount result = bankAccountService.checkBankAccountValidity(username, lastName, firstName, iban);

        // Assert
        assertNotNull(result);
        assertEquals(appUser.getId(), result.getId());
        assertEquals(lastName + " " + firstName, result.getTitle());
        assertEquals(iban, result.getIban());
    }

    @Test
    void testCheckBankAccountValidity_InvalidIban() {
        // Arrange
        String username = "username";
        String lastName = "lastName";
        String firstName = "firstName";
        String iban = "shortIban";

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setLastName(lastName);
        appUser.setFirstName(firstName);

        when(appUserService.getAppUserByUsername(username)).thenReturn(Optional.of(appUser));

        // Act & Assert
        Exception exception = assertThrows(InvalidIbanException.class, () ->
                bankAccountService.checkBankAccountValidity(username, lastName, firstName, iban)
        );

        assertEquals("Invalid Iban, Iban must be between 22 and 34 characters", exception.getMessage());
    }

    @Test
    void testCheckBankAccountValidity_AccountNotToUsersName() {
        // Arrange
        String username = "username";
        String lastName = "lastName";
        String firstName = "firstName";
        String iban = "GB82WEST12345698765432";

        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setLastName("differentLastName");
        appUser.setFirstName("differentFirstName");

        when(appUserService.getAppUserByUsername(username)).thenReturn(Optional.of(appUser));

        // Act & Assert
        Exception exception = assertThrows(AccountMustBeToUsersNameException.class, () ->
                bankAccountService.checkBankAccountValidity(username, lastName, firstName, iban)
        );

        assertEquals("Account must be to user's name", exception.getMessage());
    }

    @Test
    void testHasBankAccount_UserHasBankAccount() {
        // Arrange
        String username = "username";
        AppUser appUser = new AppUser();
        appUser.setId(1);
        BankAccount bankAccount = new BankAccount();
        bankAccount.setId(1);

        when(appUserService.getAppUserByUsername(username)).thenReturn(Optional.of(appUser));
        when(bankAccountRepository.findById(appUser.getId())).thenReturn(Optional.of(bankAccount));

        // Act
        boolean result = bankAccountService.hasBankAccount(username);

        // Assert
        assertTrue(result, "User should have a bank account");
    }

    @Test
    void testHasBankAccount_UserHasNoBankAccount() {
        // Arrange
        String username = "username";
        AppUser appUser = new AppUser();
        appUser.setId(1);

        when(appUserService.getAppUserByUsername(username)).thenReturn(Optional.of(appUser));
        when(bankAccountRepository.findById(appUser.getId())).thenReturn(Optional.empty());

        // Act
        boolean result = bankAccountService.hasBankAccount(username);

        // Assert
        assertFalse(result, "User should not have a bank account");
    }

    @Test
    void testGetAppUserBankAccount_BankAccountFound() {
        // Arrange
        int userId = 1;
        BankAccount bankAccount = new BankAccount();
        bankAccount.setId(1);

        when(bankAccountRepository.findById(userId)).thenReturn(Optional.of(bankAccount));

        // Act
        BankAccount result = bankAccountService.getAppUserBankAccount(userId);

        // Assert
        assertNotNull(result, "Bank account should not be null");
        assertEquals(bankAccount.getId(), result.getId(), "Bank account ID should match");
    }

    @Test
    void testGetAppUserBankAccount_BankAccountNotFound() {
        // Arrange
        int userId = 1;

        when(bankAccountRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        BankAccount result = bankAccountService.getAppUserBankAccount(userId);

        // Assert
        assertNotNull(result, "Bank account should not be null");
    }

}


