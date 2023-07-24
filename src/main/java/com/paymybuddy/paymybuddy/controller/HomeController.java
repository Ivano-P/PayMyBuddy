package com.paymybuddy.paymybuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//non authenticated navigation controller
@Controller
public class HomeController {

    //go to home page
    @GetMapping("/")
    public String home() {
        return "index";
    }
}
