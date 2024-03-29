package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.dto.TransactionForAppUserHistory;
import com.paymybuddy.paymybuddy.dto.TransferConfirmation;
import com.paymybuddy.paymybuddy.exceptions.InsufficientFundsException;
import com.paymybuddy.paymybuddy.exceptions.WalletNotFoundException;
import com.paymybuddy.paymybuddy.implementation.AppPmbServiceImpl;
import com.paymybuddy.paymybuddy.implementation.AppUserServiceImpl;
import com.paymybuddy.paymybuddy.implementation.TransactionServiceImpl;
import com.paymybuddy.paymybuddy.implementation.WalletServiceImpl;
import com.paymybuddy.paymybuddy.model.AccountPayMyBuddy;
import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.model.Wallet;
import com.paymybuddy.paymybuddy.repository.AccountPayMyBuddyRepository;
import com.paymybuddy.paymybuddy.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private WalletServiceImpl walletService;
    @Mock
    private AccountPayMyBuddyRepository accountPayMyBuddyRepository;
    @Mock
    private AppUserServiceImpl appUserService;
    @Mock
    private AppPmbServiceImpl appPmbService;


    @Test
    void testSaveTransaction(){
        //Arrange
        Transaction testTransaction = new Transaction();
        testTransaction.setId(1);
        testTransaction.setAmount(BigDecimal.TEN);
        testTransaction.setTransactionFee(BigDecimal.valueOf(0.01));
        testTransaction.setTransactionType(Transaction.TransactionType.SEND);
        testTransaction.setSenderId(1);
        testTransaction.setRecepientId(2);

        ArgumentCaptor<Transaction> argumentCaptor = ArgumentCaptor.forClass(Transaction.class);

        //Act
        transactionService.saveTransaction(testTransaction.getSenderId(), testTransaction
                .getRecepientId(),testTransaction.getAmount(), testTransaction
                .getTransactionFee(), testTransaction.getTransactionType(), Optional.empty() );

        //Assert
        verify(transactionRepository).save(argumentCaptor.capture());

        Transaction capturedTransaction = argumentCaptor.getValue();

        assertEquals(testTransaction.getAmount(), capturedTransaction.getAmount());
        assertEquals(testTransaction.getTransactionFee(), capturedTransaction.getTransactionFee());
        assertEquals(testTransaction.getTransactionType(), capturedTransaction.getTransactionType());
        assertEquals(testTransaction.getSenderId(), capturedTransaction.getSenderId());
        assertEquals(testTransaction.getRecepientId(), capturedTransaction.getRecepientId());

    }

    @Test
    void testTransferFunds_SuccessfulTransfer() {
        // Arrange
        String username = "TestUsername";
        int contactId = 2;
        BigDecimal amount = BigDecimal.valueOf(500);
        String description = "Test transaction";

        AppUser appUser = new AppUser();
        appUser.setId(1);

        AppUser contactAppUser = new AppUser();
        contactAppUser.setId(2);

        Wallet userWallet = new Wallet();
        userWallet.setId(1);
        userWallet.setBalance(BigDecimal.valueOf(1500));

        Wallet contactWallet = new Wallet();
        contactWallet.setId(2);
        contactWallet.setBalance(BigDecimal.valueOf(500));

        AccountPayMyBuddy pmbAccount = new AccountPayMyBuddy();
        pmbAccount.setId(1);
        pmbAccount.setBalance(BigDecimal.valueOf(1000));
        pmbAccount.setTransactionFee(0.005);

        when(appUserService.getAppUserByUsername(username)).thenReturn(Optional.of(appUser));
        when(appUserService.getAppUserById(contactId)).thenReturn(Optional.of(contactAppUser));
        when(walletService.getWalletById(appUser.getId())).thenReturn(Optional.of(userWallet));
        when(walletService.getWalletById(contactAppUser.getId())).thenReturn(Optional.of(contactWallet));
        when(appPmbService.getAccountPmb()).thenReturn(pmbAccount);

        // Act
        transactionService.transferFunds(username, contactId, amount, description);

        // Assert
        verify(walletService, times(2)).updateWallet(any(Wallet.class));
        verify(accountPayMyBuddyRepository).save(any(AccountPayMyBuddy.class));
    }


    @Test
    void testTransferFunds_InsufficientFundsException() {
        // Arrange
        String username = "TestUsername";
        int contactId = 2;
        BigDecimal amount = BigDecimal.valueOf(1500);
        String description = "Test transaction";

        AppUser appUser = new AppUser();
        appUser.setId(1);

        AppUser contactAppUser = new AppUser();
        contactAppUser.setId(2);

        Wallet userWallet = new Wallet();
        userWallet.setId(1);
        userWallet.setBalance(BigDecimal.valueOf(1000));

        Wallet contactWallet = new Wallet();
        contactWallet.setId(2);
        contactWallet.setBalance(BigDecimal.valueOf(500));

        AccountPayMyBuddy pmbAccount = new AccountPayMyBuddy();
        pmbAccount.setId(1);
        pmbAccount.setBalance(BigDecimal.valueOf(1000));
        pmbAccount.setTransactionFee(0.005);

        when(appUserService.getAppUserByUsername(username)).thenReturn(Optional.of(appUser));
        when(appUserService.getAppUserById(contactId)).thenReturn(Optional.of(contactAppUser));
        when(walletService.getWalletById(appUser.getId())).thenReturn(Optional.of(userWallet));
        when(walletService.getWalletById(contactAppUser.getId())).thenReturn(Optional.of(contactWallet));
        when(appPmbService.getAccountPmb()).thenReturn(pmbAccount);

        // Act & Assert
        assertThrows(InsufficientFundsException.class,
                () -> transactionService.transferFunds(username, contactId, amount, description));
    }

    @Test
    void testCreatTransferConfirmation() {
        // Arrange
        AppUser appUser = new AppUser();
        appUser.setId(1);
        AppUser contactAppUser = new AppUser();
        contactAppUser.setId(2);
        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.valueOf(500));
        AccountPayMyBuddy pmbAccount = new AccountPayMyBuddy();
        pmbAccount.setTransactionFee(0.005);
        when(appUserService.getAppUserByUsername("username")).thenReturn(Optional.of(appUser));
        when(appUserService.getAppUserById(2)).thenReturn(Optional.of(contactAppUser));
        when(walletService.getWalletById(1)).thenReturn(Optional.of(wallet));
        when(appPmbService.getAccountPmb()).thenReturn(pmbAccount);

        // Act
        TransferConfirmation confirmation = transactionService.creatTransferConfirmation("username", 2,
                BigDecimal.valueOf(100), Optional.of("description"));

        // Assert
        assertThat(confirmation.getSender()).isEqualTo("username");
        assertThat(confirmation.getType()).isEqualTo(Transaction.TransactionType.SEND);
        assertThat(confirmation.getRecipient()).isEqualTo(contactAppUser);
        assertThat(confirmation.getDescription()).isEqualTo("description");
        assertThat(confirmation.getTransferAmount()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(confirmation.getTransferFee().compareTo(BigDecimal.valueOf(0.50))).isZero();
        assertThat(confirmation.getAmountPlusFee().compareTo(BigDecimal.valueOf(100.50))).isZero();
        assertThat(confirmation.getBalanceAfterTransfer().compareTo(BigDecimal.valueOf(399.50))).isZero();

        verify(appUserService, times(1)).getAppUserByUsername("username");
        verify(appUserService, times(1)).getAppUserById(2);
        verify(walletService, times(1)).getWalletById(1);
        verify(appPmbService, times(1)).getAccountPmb();
    }


    @Test
    void testGetTransactionHistory() {
        // Arrange
        String username = "TestUsername";
        AppUser appUser = new AppUser();
        appUser.setId(1);
        appUser.setUsername(username);
        int userId = appUser.getId();

        // Create some transactions
        Transaction transaction1 = new Transaction();
        transaction1.setId(1);
        transaction1.setAmount(BigDecimal.TEN);
        transaction1.setTransactionFee(BigDecimal.valueOf(0.01));
        transaction1.setTransactionType(Transaction.TransactionType.SEND);
        transaction1.setSenderId(1);
        transaction1.setRecepientId(2)
        ;

        Transaction transaction2 = new Transaction();
        transaction2.setId(1);
        transaction2.setAmount(BigDecimal.valueOf(1000));
        transaction2.setTransactionFee(BigDecimal.valueOf(1));
        transaction2.setTransactionType(Transaction.TransactionType.SEND);
        transaction2.setSenderId(1);
        transaction2.setRecepientId(2);

        List<Transaction> transactionList = Arrays.asList(transaction2, transaction2);

        // Create a Page of Transactions
        Page<Transaction> transactionPage = new PageImpl<>(transactionList);

        when(appUserService.getAppUserByUsername(username)).thenReturn(Optional.of(appUser));
        when(transactionRepository.findBySenderIdOrRecepientIdOrderByIdDesc(userId, userId, Pageable.unpaged())).thenReturn(transactionPage);

        // Act
        Page<TransactionForAppUserHistory> result = transactionService.getTransactionHistory(username, Pageable.unpaged());

        // Assert
        assertThat(result.getContent()).hasSameSizeAs(transactionList);
    }

    @Test
    void testWithdrawFunds_NoUser_ThrowsException() {
        // Arrange
        String username = "unknownUser";
        BigDecimal amount = BigDecimal.valueOf(500);

        when(appUserService.getAppUserByUsername(username)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(UsernameNotFoundException.class, () -> transactionService.withdrawFunds(username, amount));
    }

    @Test
    void testWithdrawFunds_InsufficientFunds_ThrowsException() {
        // Arrange
        String username = "test";
        BigDecimal amount = BigDecimal.valueOf(1500);

        AppUser appUser = new AppUser();
        appUser.setId(1);

        Wallet userWallet = new Wallet();
        userWallet.setId(1);
        userWallet.setBalance(BigDecimal.valueOf(1000));

        when(appUserService.getAppUserByUsername(username)).thenReturn(Optional.of(appUser));
        when(walletService.getWalletById(appUser.getId())).thenReturn(Optional.of(userWallet));

        // Act and Assert
        assertThrows(InsufficientFundsException.class, () -> transactionService.withdrawFunds(username, amount));
    }

    @Test
    void testGenerateTestDeposition_Success() {
        // Arrange
        String username = "test";

        AppUser appUser = new AppUser();
        appUser.setId(1);

        Wallet userWallet = new Wallet();
        userWallet.setId(1);
        userWallet.setBalance(BigDecimal.valueOf(1500));
        //test deposite does a deposite of 1000.
        BigDecimal expectedBalance = BigDecimal.valueOf(2500);

        when(appUserService.getAppUserByUsername(username)).thenReturn(Optional.of(appUser));
        when(walletService.getWalletById(userWallet.getId())).thenReturn(Optional.of(userWallet));

        // Act
        transactionService.genarateTestDeposit(username);

        // Assert
        assertThat(userWallet.getBalance()).isEqualTo(expectedBalance);
        verify(walletService, times(1)).updateWallet(userWallet);
    }

    @Test
    void testGenerateTestDeposition_NoUser_ThrowsException() {
        // Arrange
        String username = "unknownUser";

        when(appUserService.getAppUserByUsername(username)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(UsernameNotFoundException.class, () -> transactionService.genarateTestDeposit(username));
    }

    @Test
    void testGenerateTestDeposition_NoWallet_ThrowsException() {
        // Arrange
        String username = "test";

        AppUser appUser = new AppUser();
        appUser.setId(1);

        when(appUserService.getAppUserByUsername(username)).thenReturn(Optional.of(appUser));
        when(walletService.getWalletById(appUser.getId())).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(WalletNotFoundException.class, () -> transactionService.genarateTestDeposit(username));
    }



}
