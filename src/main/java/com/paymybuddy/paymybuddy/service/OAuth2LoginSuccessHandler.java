package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.AppUser;
import com.paymybuddy.paymybuddy.model.Wallet;
import com.paymybuddy.paymybuddy.repository.AppUserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final AppUserRepository appUserRepository;
    private final AppUserService appUserService;
    private final WalletService walletService;



    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            DefaultOAuth2User user = (DefaultOAuth2User) token.getPrincipal();

            //use github users username and creat a fictional email
            String githubUsername = user.getAttribute("login");
            AppUser githubUser = new AppUser();
            AppUser finalGithubUser = githubUser;
            githubUser = appUserRepository.findByUsername(githubUsername).orElseGet(() -> {
                finalGithubUser.setUsername(githubUsername);

                walletService.creatAndLinkWallet(finalGithubUser);

                return appUserRepository.save(finalGithubUser);
            });

            /*
            githubUser = appUserRepository.findByUsername(githubUsername).orElseGet(() -> {
            githubUser.setUsername(githubUsername);
            appUserService.creatAndLinkWallet(githubUser);
            return appUserRepository.save(githubUser);
            });
             */

            // Create a list of GrantedAuthority and insert the created user's role
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + githubUser.getRole().name()));

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(githubUser.getUsername(), null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            if (response.isCommitted()) {
                return;
            }

            response.sendRedirect("/home");


        }

        //super.onAuthenticationSuccess(request, response, authentication);

    }
}
