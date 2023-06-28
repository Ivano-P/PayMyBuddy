package com.paymybuddy.paymybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogInController {

    //go to log in page
    @GetMapping("/logIn")
    public String goToLogIn() {
        return "logIn";
    }

}
