package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.BankAccount;
import com.paymybuddy.paymybuddy.service.AppUserService;
import com.paymybuddy.paymybuddy.service.BankAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

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
    private AppUserService appUserService;
    @Mock
    private BindingResult bindingResult;
    @Mock
    private BankAccountService bankAccountService;

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
    void testGoToLogIn(){
        //Act
        String viewName = userController.goToLogIn();
        //Assert
        assertThat(viewName).isEqualTo("logIn");
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
        assertThat(viewName).isEqualTo("redirect:/profile?updateSuccess=true");
        verify(appUserService, times(1)).updateAppUser(any(AppUser.class));
        assertThat(appUser.getFirstName()).isEqualTo(updatedUser.getFirstName());
        assertThat(appUser.getLastName()).isEqualTo(updatedUser.getLastName());
        assertThat(appUser.getEmail()).isEqualTo(updatedUser.getEmail());
    }

    @Test
    void testUpdatePassword() {
        //Arrange
        String currentPassword = "currentPassword";
        String newPassword = "newPassword";
        String confirmPassword = "confirmPassword";

        when(principal.getName()).thenReturn("userName");
        when(appUserService.getAppUserByUsername(any(String.class))).thenReturn(Optional.ofNullable(appUser));

        //act
        String viewName = userController.updatePassword(principal, currentPassword, newPassword, confirmPassword);

        //Assert
        verify(appUserService).updateUserPassword(appUser, currentPassword, newPassword, confirmPassword);
        verify(appUserService).updateAppUser(appUser);
        assertThat(viewName).isEqualTo("redirect:/profile?updateSuccess=true");
    }


}
