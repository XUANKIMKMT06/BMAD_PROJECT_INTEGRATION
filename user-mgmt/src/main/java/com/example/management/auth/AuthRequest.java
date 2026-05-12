package com.example.management.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AuthRequest (
        @Email(message = "Invalid email")
        @NotBlank(message = "Empty email")
        String username,

        @NotBlank(message = "Empty Password")
        String password
) {
        public AuthRequest(String username, String password){
                this.username = username.toLowerCase().trim();
                this.password = password.trim();
        }
}
