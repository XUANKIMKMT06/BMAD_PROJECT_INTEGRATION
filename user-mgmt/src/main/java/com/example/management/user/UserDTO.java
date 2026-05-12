package com.example.management.user;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public record UserDTO(String name, String username, List<SimpleGrantedAuthority> authorities) {}
