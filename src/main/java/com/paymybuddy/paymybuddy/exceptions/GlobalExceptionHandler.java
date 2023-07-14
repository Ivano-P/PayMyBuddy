package com.paymybuddy.paymybuddy.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERRORMESSAGE = "errorMsg";

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElementException(NoSuchElementException nsee, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERRORMESSAGE, nsee.getMessage());
        return "redirect:/contact";
    }

    @ExceptionHandler(AccountMustBeToUsersNameException.class)
    public String handleAccountMustBeToUsersNameException(AccountMustBeToUsersNameException ambtune,
                                                          RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERRORMESSAGE, ambtune.getMessage());
        return "redirect:/profile";
    }

    @ExceptionHandler(InvalidIbanException.class)
    public String handleInvalidIbanException(InvalidIbanException iie,
                                                          RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERRORMESSAGE, iie.getMessage());
        return "redirect:/profile";
    }

    @ExceptionHandler(NoBankAccountException.class)
    public String handleNoBankAccountException(NoBankAccountException nbae,
                                             RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERRORMESSAGE, nbae.getMessage());
        return "redirect:/transfer";
    }

    @ExceptionHandler(MissingUserInfoException.class)
    public String handleMissingUserInfoException(MissingUserInfoException muie,
                                             RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERRORMESSAGE, muie.getMessage());
        return "redirect:/update_profile";
    }

}
