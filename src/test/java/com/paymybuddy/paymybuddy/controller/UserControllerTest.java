package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.dto.TransactionForAppUserHistory;
import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.BankAccount;
import com.paymybuddy.paymybuddy.implementation.AppPmbServiceImpl;
import com.paymybuddy.paymybuddy.implementation.AppUserServiceImpl;
import com.paymybuddy.paymybuddy.implementation.BankAccountServiceImpl;
import com.paymybuddy.paymybuddy.implementation.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @InjectMocks
    private UserController userController;
    @Mock
    private AppUserServiceImpl appUserService;
    @Mock
    private BindingResult bindingResult;

    @Mock
    private BankAccountServiceImpl bankAccountService;

    @Mock
    private TransactionServiceImpl transactionService;

    @Mock
    private AppPmbServiceImpl appPmbService;


    private AppUser appUser;
    private Principal principal;

    @BeforeEach
    void setUp() {
        appUser = new AppUser();
        appUser.setFirstName("John");
        appUser.setLastName("Doe");
        appUser.setEmail("john.doe@example.com");

        principal = mock(Principal.class);
    }

    @Test
    void testRegisterAppUser_userDoesNotExist() {
        // Arrange
        when(appUserService.createAppUser(any(AppUser.class))).thenReturn(appUser);

        // Act
        String viewName = userController.registerAppUser(appUser, bindingResult);

        // Assert
        assertThat(viewName).isEqualTo("registrationSuccessful");
    }

    @Test
    void testRegisterAppUser_userAlreadyExists() {
        // Arrange
        when(appUserService.createAppUser(any(AppUser.class))).thenReturn(null);

        // Act
        String viewName = userController.registerAppUser(appUser, bindingResult);

        // Assert
        assertThat(viewName).isEqualTo("registrationFailure");
    }

    @Test
    void testGoToHomePage() {
        // Arrange
        when(principal.getName()).thenReturn("username");
        when(appUserService.getAppUserByUsername(any(String.class))).thenReturn(Optional.ofNullable(appUser));
        doNothing().when(appUserService).checkIfAllUserInfoPresent(any(AppUser.class));
        Model model = new ExtendedModelMap();

        // Act
        String viewName = userController.goToHomePage(model, principal);

        // Assert
        assertThat(viewName).isEqualTo("home");
        assertThat(model.containsAttribute("currentUser")).isTrue();
        assertThat(model.getAttribute("currentUser")).isEqualTo(appUser);
    }

    @Test
    void testGoToTransferPage() {
        // Arrange
        when(principal.getName()).thenReturn("username");
        when(appUserService.getAppUserByUsername(any(String.class))).thenReturn(Optional.ofNullable(appUser));
        doNothing().when(appUserService).checkIfAllUserInfoPresent(any(AppUser.class));
        when(appUserService.getContactsForUser(any(AppUser.class))).thenReturn(Collections.emptyList());
        when(bankAccountService.hasBankAccount(any(String.class))).thenReturn(true);
        Page<TransactionForAppUserHistory> transactions = new PageImpl<>(Collections.emptyList());
        when(transactionService.getTransactionHistory(any(String.class), any(PageRequest.class))).thenReturn(transactions);
        Model model = new ExtendedModelMap();

        // Act
        String viewName = userController.goToTransferPage(model, principal, 0);

        // Assert
        assertThat(viewName).isEqualTo("transfer");
        assertThat(model.containsAttribute("currentUser")).isTrue();
        assertThat(model.getAttribute("currentUser")).isEqualTo(appUser);
        assertThat(model.containsAttribute("contacts")).isTrue();
        assertThat(model.containsAttribute("hasBankAccount")).isTrue();
        assertThat(model.containsAttribute("transactions")).isTrue();
        assertThat(model.containsAttribute("totalPages")).isTrue();
        assertThat(model.containsAttribute("currentPage")).isTrue();
    }

    @Test
    void testGoToIban() {
        // Arrange
        when(principal.getName()).thenReturn("username");
        when(appUserService.getAppUserByUsername(any(String.class))).thenReturn(Optional.ofNullable(appUser));
        when(appPmbService.getPmbIban()).thenReturn("iban");
        Model model = new ExtendedModelMap();

        // Act
        String viewName = userController.goToIban(model, principal);

        // Assert
        assertThat(viewName).isEqualTo("iban");
        assertThat(model.containsAttribute("currentUser")).isTrue();
        assertThat(model.getAttribute("currentUser")).isEqualTo(appUser);
        assertThat(model.containsAttribute("iban")).isTrue();
        assertThat(model.getAttribute("iban")).isEqualTo("iban");
    }

    @Test
    void testDepositFunds() {
        // Act
        String viewName = userController.depositFunds();

        // Assert
        assertThat(viewName).isEqualTo("redirect:/iban");
    }

    @Test
    void testGoToProfilePage() {
        // Arrange
        when(principal.getName()).thenReturn("username");
        when(appUserService.getAppUserByUsername(any(String.class))).thenReturn(Optional.ofNullable(appUser));
        doNothing().when(appUserService).checkIfAllUserInfoPresent(any(AppUser.class));
        when(bankAccountService.hasBankAccount(any(String.class))).thenReturn(true);
        BankAccount bankAccount = new BankAccount();
        when(bankAccountService.getAppUserBankAccount(any(Integer.class))).thenReturn(bankAccount);
        Model model = new ExtendedModelMap();

        // Act
        String viewName = userController.goToProfilePage(model, principal);

        // Assert
        assertThat(viewName).isEqualTo("profile");
        assertThat(model.containsAttribute("currentUser")).isTrue();
        assertThat(model.getAttribute("currentUser")).isEqualTo(appUser);
        assertThat(model.containsAttribute("hasBankAccount")).isTrue();
        assertThat(model.containsAttribute("bankAccount")).isTrue();
    }

    @Test
    void testGoToContactPage() {
        // Arrange
        when(principal.getName()).thenReturn("username");
        when(appUserService.getAppUserByUsername(any(String.class))).thenReturn(Optional.ofNullable(appUser));
        doNothing().when(appUserService).checkIfAllUserInfoPresent(any(AppUser.class));
        when(appUserService.getContactsForUser(any(AppUser.class))).thenReturn(Collections.emptyList());
        Model model = new ExtendedModelMap();

        // Act
        String viewName = userController.goToContactPage(model, principal);

        // Assert
        assertThat(viewName).isEqualTo("contact");
        assertThat(model.containsAttribute("currentUser")).isTrue();
        assertThat(model.getAttribute("currentUser")).isEqualTo(appUser);
        assertThat(model.containsAttribute("contacts")).isTrue();
    }

    @Test
    void testAddContact() {
        // Arrange
        String contactUsername = "contactUsername";
        when(principal.getName()).thenReturn("username");
        doNothing().when(appUserService).addContact(any(String.class), any(String.class));

        // Act
        String viewName = userController.addContact(principal, contactUsername);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/contact");
    }

    @Test
    void testRemoveContact() {
        // Arrange
        Integer contactId = 1;
        when(principal.getName()).thenReturn("username");
        doNothing().when(appUserService).removeContact(any(String.class), any(Integer.class));

        // Act
        String viewName = userController.removeContact(principal, contactId);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/contact");
    }

    @Test
    void testTransferFunds() {
        // Arrange
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("username");
        doNothing().when(transactionService).transferFunds(any(String.class), anyInt(), any(BigDecimal.class), any(String.class));

        // Act
        String viewName = userController.transferFunds(principal, 1, BigDecimal.TEN, "description");

        // Assert
        assertThat(viewName).isEqualTo("redirect:/transfer");
        verify(transactionService, times(1)).transferFunds(any(String.class), anyInt(), any(BigDecimal.class), any(String.class));
    }

// Do similar for other methods

    @Test
    void testGoToUpdateProfileInfoPage() {
        // Arrange
        when(principal.getName()).thenReturn("username");
        when(appUserService.getAppUserByUsername(any(String.class))).thenReturn(Optional.ofNullable(appUser));
        Model model = new ExtendedModelMap();

        // Act
        String viewName = userController.goToUpdateProfileInfoPage(model, principal);

        // Assert
        assertThat(viewName).isEqualTo("update_profile");
        assertThat(model.containsAttribute("currentUser")).isTrue();
        assertThat(model.containsAttribute("appUser")).isTrue();
        assertThat(model.getAttribute("currentUser")).isEqualTo(appUser);
    }

    @Test
    void testUpdateProfileInfo() {
        // Arrange
        AppUser updatedUser = new AppUser();
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");
        updatedUser.setEmail("updated.user@example.com");

        when(principal.getName()).thenReturn("username");
        when(appUserService.getAppUserByUsername(any(String.class))).thenReturn(Optional.ofNullable(appUser));
        doNothing().when(appUserService).updateAppUser(any(AppUser.class));

        // Act
        String viewName = userController.updateProfileInfo(updatedUser, principal);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/profile");
        verify(appUserService, times(1)).updateAppUser(any(AppUser.class));
        assertThat(appUser.getFirstName()).isEqualTo(updatedUser.getFirstName());
        assertThat(appUser.getLastName()).isEqualTo(updatedUser.getLastName());
        assertThat(appUser.getEmail()).isEqualTo(updatedUser.getEmail());
    }

}
