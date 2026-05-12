package com.example.management.security.jwt;

import com.example.management.user.AppUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Jwt {
    private Integer id;
    private String token;
    private AppUser user;
    private boolean loggedOut;

    public Jwt(String token, AppUser user) {
        this.token = token;
        this.user = user;
        this.loggedOut = false;
    }
}
