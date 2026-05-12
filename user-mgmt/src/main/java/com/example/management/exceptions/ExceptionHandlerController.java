package com.example.management.exceptions;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleException(AuthenticationException e) {
        var eStatus = e.getClass().getAnnotation(ResponseStatus.class);
        HttpStatus status = eStatus != null? eStatus.code() : HttpStatus.UNAUTHORIZED;
        return ResponseEntity
                .status(status)
                .body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("\n "));
        return ResponseEntity
                .badRequest()
                .body(errorMessage);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFound(){
        return "forward:/views/404.html";
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e){
        var eStatus = e.getClass().getAnnotation(ResponseStatus.class);
        HttpStatus status = eStatus != null? eStatus.code() : HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(status)
                .body(e.getMessage());
    }

}

