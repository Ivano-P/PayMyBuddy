package com.paymybuddy.paymybuddy.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotLoggedInNavigationController.class)
class NotLoggedInNavigationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
    }

    @Test
    @WithMockUser
    void givenAuthRequest_onLogIn_shouldReturnStatus200() throws Exception {
        mockMvc.perform(get("/logIn"))
                .andExpect(status().isOk());
    }


    @Test
    void givenAuthRequest_onLogIn_shouldReturnStatus302() throws Exception {
        mockMvc.perform(get("/logIn"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost/oauth2/authorization/github"));
    }

}
