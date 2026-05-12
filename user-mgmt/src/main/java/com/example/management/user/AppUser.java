package com.example.management.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Builder
public class AppUser implements UserDetails {

    @Getter
    @Setter
    private Integer id;

    @Getter
    @NotBlank(message = "Empty name")
    private String name;

    @Email(message = "Invalid email")
    @NotBlank(message = "Empty email")
    private String username;

    @NotBlank(message = "Empty password")
    private String password;

    @Setter
    @NotEmpty
    private List<SimpleGrantedAuthority> authorities;

    public AppUser(Integer id, String name, String username, String password, List<SimpleGrantedAuthority> authorities) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    @Override
    public List<SimpleGrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }
}
