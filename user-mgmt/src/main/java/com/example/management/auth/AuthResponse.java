package com.example.management.auth;

import com.example.management.user.UserDTO;

public record AuthResponse(UserDTO user, String token) {}
