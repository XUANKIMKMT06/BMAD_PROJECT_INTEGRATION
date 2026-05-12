package com.example.management.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
public class InvalidJwtException extends InsufficientAuthenticationException {
    public InvalidJwtException(String msg) {
        super(msg);
    }
}
