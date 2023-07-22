package com.paymybuddy.paymybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//non authenticated navigation controller
@Controller
public class NotLoggedInNavigationController {

    //go to home page
    @GetMapping("/")
    public String home() {
        return "index";
    }

    //go to log in page
    @GetMapping("/logIn")
    public String goToLogIn() {
        return "logIn";
    }


}
