package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.implementation.AppPmbServiceImpl;
import com.paymybuddy.paymybuddy.implementation.AppUserServiceImpl;
import com.paymybuddy.paymybuddy.model.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankAccountControllerTest {
    @InjectMocks
    private BankAccountController bankAccountController;

    @Mock
    private AppUserServiceImpl appUserService;

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
    void testGoToIban() {
        // Arrange
        when(principal.getName()).thenReturn("username");
        when(appUserService.getAppUserByUsername(any(String.class))).thenReturn(Optional.ofNullable(appUser));
        when(appPmbService.getPmbIban()).thenReturn("iban");
        Model model = new ExtendedModelMap();

        // Act
        String viewName = bankAccountController.goToIban(model, principal);

        // Assert
        assertThat(viewName).isEqualTo("iban");
        assertThat(model.containsAttribute("currentUser")).isTrue();
        assertThat(model.getAttribute("currentUser")).isEqualTo(appUser);
        assertThat(model.containsAttribute("iban")).isTrue();
        assertThat(model.getAttribute("iban")).isEqualTo("iban");
    }

}
