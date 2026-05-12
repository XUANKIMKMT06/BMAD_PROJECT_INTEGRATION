package com.example.management.security.jwt;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthFilter.class);

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) {

        try {
            String authHeader = request.getHeader("Authorization");
            String token;
            String username;
            // log.info("Someone trying to auth");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // log.info("No auth header found");
                filterChain.doFilter(request, response);
                return;
            }
            // log.info("Auth header found -> Header: {}", authHeader);
            token = authHeader.substring(7);
            username = jwtService.extractUsername(token);
            if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                // log.info("Username is null or already authenticated");
                filterChain.doFilter(request, response);
                return;
            }

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            Jwt jwt = this.jwtService.getToken(token);
            if (jwtService.isValidToken(jwt, userDetails)) {
                // log.info("Token is valid");
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException | AuthenticationException e) {
            log.error("Error while filtering request: ", e);
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }
}
