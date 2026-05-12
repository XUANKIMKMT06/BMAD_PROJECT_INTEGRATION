package com.example.management.security;

import com.example.management.auth.AuthRequest;
import com.example.management.security.jwt.JwtAuthenticationFailureHandler;
import com.example.management.security.jwt.JwtAuthenticationSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonUsernamePasswordAuthFilter extends UsernamePasswordAuthenticationFilter {

    public JsonUsernamePasswordAuthFilter(AuthenticationManager authenticationManager, JwtAuthenticationFailureHandler failureHandler, JwtAuthenticationSuccessHandler successHandler) {
        super(authenticationManager);
        setAuthenticationFailureHandler(failureHandler);
        setAuthenticationSuccessHandler(successHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        if (request.getContentType().equals(MediaType.APPLICATION_JSON_VALUE)) {
            ObjectMapper map = new ObjectMapper();
            try {
                AuthRequest authRequest = map.readValue(request.getInputStream(), AuthRequest.class);
                UsernamePasswordAuthenticationToken authToken = UsernamePasswordAuthenticationToken.unauthenticated(
                        authRequest.username(),
                        authRequest.password()
                );
                return this.getAuthenticationManager().authenticate(authToken);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return super.attemptAuthentication(request, response);
    }
}
