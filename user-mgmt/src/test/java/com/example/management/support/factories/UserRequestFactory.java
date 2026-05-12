package com.example.management.support.factories;

import com.example.management.auth.AuthRequest;
import com.example.management.auth.RegisterRequest;

import java.util.Map;
import java.util.UUID;

public final class UserRequestFactory {

    private UserRequestFactory() {
    }

    public static RegisterRequest registerRequest() {
        return registerRequest(Map.of());
    }

    public static RegisterRequest registerRequest(Map<String, String> overrides) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String name = overrides.getOrDefault("name", "Test User " + suffix);
        String username = overrides.getOrDefault("username", "user-" + suffix + "@test.local");
        String password = overrides.getOrDefault("password", "Password1");
        return new RegisterRequest(name, username, password);
    }

    public static AuthRequest authRequest(RegisterRequest registerRequest) {
        return new AuthRequest(registerRequest.username(), registerRequest.password());
    }

    public static AuthRequest authRequest(Map<String, String> overrides) {
        RegisterRequest registerRequest = registerRequest(overrides);
        return authRequest(registerRequest);
    }
}
