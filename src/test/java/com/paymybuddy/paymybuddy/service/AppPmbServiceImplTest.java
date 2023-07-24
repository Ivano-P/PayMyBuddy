package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.AccountPayMyBuddy;
import com.paymybuddy.paymybuddy.repository.AccountPayMyBuddyRepository;
import com.paymybuddy.paymybuddy.implementation.AppPmbServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class AppPmbServiceImplTest {

    @InjectMocks
    private AppPmbServiceImpl appPmbService;
    @Mock
    private AccountPayMyBuddyRepository accountPayMyBuddyRepository;
    
    @Test
    void testGetAccountPmb_AccountExists() {
        AccountPayMyBuddy mockAccount = new AccountPayMyBuddy();
        mockAccount.setBalance(BigDecimal.ZERO);
        when(accountPayMyBuddyRepository.findById(AppPmbServiceImpl.PMB_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));

        AccountPayMyBuddy result = appPmbService.getAccountPmb();

        assertThat(result).isEqualTo(mockAccount);
    }

    @Test
    void testGetAccountPmb_AccountDoesNotExist() {
        when(accountPayMyBuddyRepository.findById(AppPmbServiceImpl.PMB_ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appPmbService.getAccountPmb()).isInstanceOf(RuntimeException.class);
    }

    @Test
    void testCheckIfPmbAccountIsPresent_AccountExists() {
        AccountPayMyBuddy mockAccount = new AccountPayMyBuddy();
        mockAccount.setBalance(BigDecimal.ZERO);
        when(accountPayMyBuddyRepository.findById(AppPmbServiceImpl.PMB_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));

        boolean result = appPmbService.checkIfPmbAccountIsPresent();

        assertThat(result).isTrue();
    }

    @Test
    void testCheckIfPmbAccountIsPresent_AccountDoesNotExist() {
        when(accountPayMyBuddyRepository.findById(AppPmbServiceImpl.PMB_ACCOUNT_ID)).thenReturn(Optional.empty());

        boolean result = appPmbService.checkIfPmbAccountIsPresent();

        assertThat(result).isFalse();
    }

    @Test
    void testCreatPmbAccount_AccountDoesNotExist() {
        when(accountPayMyBuddyRepository.findById(AppPmbServiceImpl.PMB_ACCOUNT_ID)).thenReturn(Optional.empty());

        appPmbService.creatPmbAccount();

        verify(accountPayMyBuddyRepository, times(1)).save(any(AccountPayMyBuddy.class));
    }

    @Test
    void testCreatPmbAccount_AccountExists() {
        AccountPayMyBuddy mockAccount = new AccountPayMyBuddy();
        mockAccount.setBalance(BigDecimal.ZERO);
        when(accountPayMyBuddyRepository.findById(AppPmbServiceImpl.PMB_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));

        appPmbService.creatPmbAccount();

        verify(accountPayMyBuddyRepository, never()).save(any(AccountPayMyBuddy.class));
    }

    @Test
    void testGetPmbIban() {
        AccountPayMyBuddy mockAccount = new AccountPayMyBuddy();
        mockAccount.setBalance(BigDecimal.ZERO);
        mockAccount.setIban("DE89 3704 0044 0532 0130 00");
        when(accountPayMyBuddyRepository.findById(AppPmbServiceImpl.PMB_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));

        String result = appPmbService.getPmbIban();

        assertThat(result).isEqualTo(mockAccount.getIban());
    }
}
