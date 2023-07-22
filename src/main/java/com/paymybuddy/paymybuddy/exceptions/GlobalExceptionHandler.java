package com.paymybuddy.paymybuddy.exceptions;

import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_MESSAGE = "errorMsg";
    private static final String REDIRECT_TRANSFER = "redirect:/transfer";
    private static final String REDIRECT_CONTACT = "redirect:/contact";
    private static final String REDIRECT_PROFILE = "redirect:/profile";


    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElementException(NoSuchElementException nsee, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, nsee.getMessage());
        return REDIRECT_CONTACT;
    }

    @ExceptionHandler(ContactNotFoundException.class)
    public String handleContactNotFoundException(ContactNotFoundException cnfe,
                                                 RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, cnfe.getMessage());
        return REDIRECT_CONTACT;
    }

    @ExceptionHandler(AccountMustBeToUsersNameException.class)
    public String handleAccountMustBeToUsersNameException(AccountMustBeToUsersNameException ambtune,
                                                          RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, ambtune.getMessage());
        return REDIRECT_PROFILE;
    }

    @ExceptionHandler(InvalidIbanException.class)
    public String handleInvalidIbanException(InvalidIbanException iie,
                                             RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, iie.getMessage());
        return REDIRECT_PROFILE;
    }

    @ExceptionHandler(MissingUserInfoException.class)
    public String handleMissingUserInfoException(MissingUserInfoException muie,
                                                 RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, muie.getMessage());
        return "redirect:/update_profile";
    }

    @ExceptionHandler(NoBankAccountException.class)
    public String handleNoBankAccountException(NoBankAccountException nbae,
                                               RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, nbae.getMessage());
        return REDIRECT_TRANSFER;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleMissingServletRequestParameterException(RedirectAttributes redirectAttributes) {
        redirectAttributes
                .addFlashAttribute(ERROR_MESSAGE, "contact and amount must be defined for transfer");
        return REDIRECT_TRANSFER;
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public String handleInsufficientFundsException(InsufficientFundsException ife,
                                                   RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, ife.getMessage());
        return REDIRECT_TRANSFER;
    }

    @ExceptionHandler(NoContactSelectedException.class)
    public String handleNoContactSelectedException(NoContactSelectedException ncse,
                                                   RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, ncse.getMessage());
        return REDIRECT_TRANSFER;
    }

    @ExceptionHandler(InvalidAmountException.class)
    public String handleInvalidAmountException(InvalidAmountException iae,
                                               RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, iae.getMessage());
        return REDIRECT_TRANSFER;
    }

}
