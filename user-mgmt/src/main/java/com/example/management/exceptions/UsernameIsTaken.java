package com.example.management.exceptions;

import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class UsernameIsTaken extends ValidationException {
    public UsernameIsTaken(String message) {
        super(message);
    }
}
