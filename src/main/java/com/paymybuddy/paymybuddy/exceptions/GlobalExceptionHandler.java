package com.paymybuddy.paymybuddy.exceptions;

import jakarta.transaction.InvalidTransactionException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleMissingServletRequestParameterException(RedirectAttributes redirectAttributes) {
        redirectAttributes
                .addFlashAttribute(ERRORMESSAGE, "contact and amount must be defined for transfer");
        return "redirect:/transfer";
    }

    @ExceptionHandler(MissingUserInfoException.class)
    public String handleMissingUserInfoException(MissingUserInfoException muie,
                                             RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERRORMESSAGE, muie.getMessage());
        return "redirect:/update_profile";
    }

    @ExceptionHandler(ContactNotFoundException.class)
    public String handleContactNotFoundException(ContactNotFoundException cnfe,
                                             RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERRORMESSAGE, cnfe.getMessage());
        return "redirect:/contact";
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public String handleInsufficientFundsException(InsufficientFundsException ife,
                                                   RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERRORMESSAGE, ife.getMessage());
        return "redirect:/transfer";
    }




}
