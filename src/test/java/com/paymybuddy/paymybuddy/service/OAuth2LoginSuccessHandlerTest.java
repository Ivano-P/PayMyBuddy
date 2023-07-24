package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.implementation.WalletServiceImpl;
import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private WalletServiceImpl walletService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private OAuth2LoginSuccessHandler handler;

    @Test
    void onAuthenticationSuccess() throws Exception {
        // Arrange
        AppUser appUser = new AppUser();
        appUser.setUsername("username");

        DefaultOAuth2User oAuth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("login", "username", "name", "nameValue"), "name");
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(oAuth2User,
                List.of(), "registrationId");

        when(appUserRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(appUser));

        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(walletService, never()).creatAndLinkWallet(any());
        verify(appUserRepository, never()).save(any());
        verify(securityContext).setAuthentication(any());
    }
}

