package com.example.management.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class UserDoesNotExistException extends UsernameNotFoundException {
    public UserDoesNotExistException(String message) {
        super(message);
    }
}
