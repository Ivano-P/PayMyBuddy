package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

@SpringBootTest
class OAuth2LoginSuccessHandlerTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private WalletService walletService;

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

