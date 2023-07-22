package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.Wallet;
import com.paymybuddy.paymybuddy.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @InjectMocks
    WalletService walletService;
    @Mock
    private WalletRepository walletRepository;

    @Test
    void testGetWalletById() {
        Wallet expectedWallet = new Wallet();
        expectedWallet.setId(1);
        expectedWallet.setBalance(BigDecimal.valueOf(1500));

        when(walletRepository.findById(expectedWallet.getId())).thenReturn(Optional.of(expectedWallet));

        Optional<Wallet> actualWallet = walletService.getWalletById(expectedWallet.getId());

        assertThat(actualWallet).isPresent().contains(expectedWallet);;
    }

    @Test
    void testGetAllWallets() {
        Wallet wallet1 = new Wallet();
        wallet1.setId(1);
        wallet1.setBalance(BigDecimal.valueOf(1500));

        Wallet wallet2 = new Wallet();
        wallet2.setId(2);
        wallet2.setBalance(BigDecimal.valueOf(3000));

        List<Wallet> expectedWallets = Arrays.asList(wallet1, wallet2);

        when(walletRepository.findAll()).thenReturn(expectedWallets);

        List<Wallet> actualWallets = walletService.getAllWallet();

        //assertEquals(expectedWallets, actualWallets);
        assertThat(actualWallets).isEqualTo(expectedWallets);
    }

    @Test
    void testUpdateWallet() {
        Wallet wallet = new Wallet();
        wallet.setId(1);
        wallet.setBalance(BigDecimal.valueOf(1500));

        walletService.updateWallet(wallet);

        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    void testSetAdminUserWalletBalance() {
        //Arrange
        AppUser testAdminUser = new AppUser();
        testAdminUser.setId(1);
        testAdminUser.setUsername("testUser");

        //Act
        walletService.setAdminUserWalletBalance(testAdminUser);

        Wallet userWallet = testAdminUser.getWallet();

        //Assert
        assertNotNull(userWallet);
        assertEquals(BigDecimal.valueOf(10000), userWallet.getBalance());
        assertEquals(testAdminUser, userWallet.getAppUser());
    }

    @Test
    void testCreatAndLinkWallet() {
        //Arrange
        AppUser testUser = new AppUser();
        testUser.setId(1);
        testUser.setUsername("testUser");

        //Act
        walletService.creatAndLinkWallet(testUser);

        //Assert
        Wallet userWallet = testUser.getWallet();
        assertNotNull(userWallet);
        assertEquals(BigDecimal.ZERO, userWallet.getBalance());
        assertEquals(testUser, userWallet.getAppUser());
    }


}
