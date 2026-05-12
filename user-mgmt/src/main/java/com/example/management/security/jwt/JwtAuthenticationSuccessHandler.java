package com.example.management.security.jwt;

import com.example.management.auth.AuthResponse;
import com.example.management.user.AppUser;
import com.example.management.user.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    JwtService jwtService;
    ObjectMapper objectMapper;

    public JwtAuthenticationSuccessHandler(JwtService jwtService) {
        this.jwtService = jwtService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        var principal = authentication.getPrincipal();
        UserDTO userDTO = null;
        String token = "";
        if (principal instanceof AppUser user) {
            userDTO = new UserDTO(user.getName(), user.getUsername(), user.getAuthorities());
            token = jwtService.generateToken(user);
        }

        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new AuthResponse(userDTO, token)));
        response.getWriter().flush();

    }
}
