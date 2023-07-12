package com.paymybuddy.paymybuddy.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElementException(NoSuchElementException nsee, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMsg", "Contact not found");
        return "redirect:/contact";
    }

    @ExceptionHandler(AccountMustBeToUsersNameException.class)
    public String handleAccountMustBeToUsersNameException(AccountMustBeToUsersNameException ambtune,
                                                          RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMsg", "Account must be to user's name");
        return "redirect:/profile";
    }

    @ExceptionHandler(InvalidIbanException.class)
    public String handleInvalidIbanException(InvalidIbanException iie,
                                                          RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMsg", "Invalid Iban, Iban must be between " +
                "22 and 34 characters");
        return "redirect:/profile";
    }
}
