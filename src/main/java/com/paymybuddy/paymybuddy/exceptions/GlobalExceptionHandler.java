package com.paymybuddy.paymybuddy.exceptions;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_MESSAGE = "errorMsg";
    private static final String REDIRECT_TRANSFER = "redirect:/transfer";
    private static final String REDIRECT_CONTACT = "redirect:/contact";
    private static final String REDIRECT_PROFILE = "redirect:/profile";


    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElementException(NoSuchElementException nsee, RedirectAttributes redirectAttributes) {
        log.error("NoSuchElementException thrown: {} " , nsee.getMessage(), nsee);
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, nsee.getMessage());
        return REDIRECT_CONTACT;
    }

    @ExceptionHandler(ContactNotFoundException.class)
    public String handleContactNotFoundException(ContactNotFoundException cnfe,
                                                 RedirectAttributes redirectAttributes) {
        log.error("ContactNotFoundException thrown: {} " , cnfe.getMessage(), cnfe);
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, cnfe.getMessage());
        return REDIRECT_CONTACT;
    }

    @ExceptionHandler(AccountMustBeToUsersNameException.class)
    public String handleAccountMustBeToUsersNameException(AccountMustBeToUsersNameException ambtune,
                                                          RedirectAttributes redirectAttributes) {
        log.error("AccountMustBeToUsersNameException thrown: {} " , ambtune.getMessage(), ambtune);
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, ambtune.getMessage());
        return REDIRECT_PROFILE;
    }

    @ExceptionHandler(InvalidIbanException.class)
    public String handleInvalidIbanException(InvalidIbanException iie,
                                             RedirectAttributes redirectAttributes) {
        log.error("InvalidIbanException thrown: {} " , iie.getMessage(), iie);
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, iie.getMessage());
        return REDIRECT_PROFILE;
    }

    @ExceptionHandler(MissingUserInfoException.class)
    public String handleMissingUserInfoException(MissingUserInfoException muie,
                                                 RedirectAttributes redirectAttributes) {
        log.error("MissingUserInfoException thrown: {} " , muie.getMessage(), muie);
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, muie.getMessage());
        return "redirect:/update_profile";
    }

    @ExceptionHandler(NoBankAccountException.class)
    public String handleNoBankAccountException(NoBankAccountException nbae,
                                               RedirectAttributes redirectAttributes) {
        log.error("NoBankAccountException thrown: {} " , nbae.getMessage(), nbae);
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, nbae.getMessage());
        return REDIRECT_TRANSFER;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleMissingServletRequestParameterException(RedirectAttributes redirectAttributes) {
        log.error("MissingServletRequestParameterException thrown");
        redirectAttributes
                .addFlashAttribute(ERROR_MESSAGE, "contact and amount must be defined for transfer");
        return REDIRECT_TRANSFER;
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public String handleInsufficientFundsException(InsufficientFundsException ife,
                                                   RedirectAttributes redirectAttributes) {
        log.error("InsufficientFundsException thrown: {} " , ife.getMessage(), ife);
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, ife.getMessage());
        return REDIRECT_TRANSFER;
    }

    @ExceptionHandler(NoContactSelectedException.class)
    public String handleNoContactSelectedException(NoContactSelectedException ncse,
                                                   RedirectAttributes redirectAttributes) {
        log.error("NoContactSelectedException thrown: {} " , ncse.getMessage(), ncse);
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, ncse.getMessage());
        return REDIRECT_TRANSFER;
    }

    @ExceptionHandler(InvalidAmountException.class)
    public String handleInvalidAmountException(InvalidAmountException iae,
                                               RedirectAttributes redirectAttributes) {
        log.error("InvalidAmountException thrown: {} " , iae.getMessage(), iae);
        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, iae.getMessage());
        return REDIRECT_TRANSFER;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleMethodArgumentNotValidException(MethodArgumentNotValidException manve,
                                                        RedirectAttributes redirectAttributes){
        log.error("MethodArgumentNotValidException thrown: {} ", manve.getMessage(), manve);

        BindingResult result = manve.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        StringBuilder errorMessage = new StringBuilder();
        fieldErrors.forEach(fe -> errorMessage.append(fe.getField()).append(" ").append(fe.getDefaultMessage())
                .append("\n"));

        redirectAttributes.addFlashAttribute(ERROR_MESSAGE, errorMessage.toString());

        return "redirect:/registrationFailure";
    }
}
