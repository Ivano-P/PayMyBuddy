package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.dto.TransactionForAppUserHistory;
import com.paymybuddy.paymybuddy.implementation.AppPmbServiceImpl;
import com.paymybuddy.paymybuddy.implementation.AppUserServiceImpl;
import com.paymybuddy.paymybuddy.implementation.BankAccountServiceImpl;
import com.paymybuddy.paymybuddy.implementation.TransactionServiceImpl;
import com.paymybuddy.paymybuddy.model.AppUser;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {
    @InjectMocks
    private TransactionController transactionController;
    @Mock
    private AppUserServiceImpl appUserService;
    @Mock
    private BankAccountServiceImpl bankAccountService;
    @Mock
    private TransactionServiceImpl transactionService;
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
        String viewName = transactionController.goToTransferPage(model, principal, 0);

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
    void testDepositFunds() {
        // Act
        String viewName = transactionController.depositFunds();

        // Assert
        assertThat(viewName).isEqualTo("redirect:/iban");
    }

    @Test
    void testTransferFunds() {
        // Arrange
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("username");
        doNothing().when(transactionService).transferFunds(any(String.class), anyInt(), any(BigDecimal.class), any(String.class));

        // Act
        String viewName = transactionController.transferFunds(principal, 1, BigDecimal.TEN, "description");

        // Assert
        assertThat(viewName).isEqualTo("redirect:/transfer?transferSuccess=true");
        verify(transactionService, times(1)).transferFunds(any(String.class), anyInt(), any(BigDecimal.class), any(String.class));
    }

}
