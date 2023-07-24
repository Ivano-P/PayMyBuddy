package com.paymybuddy.paymybuddy.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;

@Service
public interface WebSecurityConfig {
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception;
}
