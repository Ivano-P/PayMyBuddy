package com.paymybuddy.paymybuddy.implementation;

import com.paymybuddy.paymybuddy.security.WebSecurityConfig;
import com.paymybuddy.paymybuddy.service.OAuth2LoginSuccessHandler;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class WebSecurityConfigImpl implements WebSecurityConfig {

    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        //set my customized login page for spring security and makes it accessible without being logged-in
        httpSecurity.formLogin().loginPage("/logIn").defaultSuccessUrl("/home", true )
                .permitAll();
        //set pages that are accessible without being logged in
        httpSecurity.authorizeHttpRequests()
                .requestMatchers("/", "/register", "/registrationSuccessful", "registrationFailure")
                .permitAll();
        //to make admin pages accessible only to admin users.
        httpSecurity.authorizeHttpRequests().requestMatchers("/admin/**").hasRole("ADMIN");

        httpSecurity.authorizeHttpRequests().anyRequest().authenticated();

        httpSecurity.logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID");

        httpSecurity.oauth2Login()
                .successHandler(oAuth2LoginSuccessHandler);

        //to check user in db for login
        httpSecurity.userDetailsService(customUserDetailsService);

        return httpSecurity.build();
    }


}
