package com.example.management.app;

import com.example.management.exceptions.UserDoesNotExistException;
import com.example.management.user.AppUser;
import com.example.management.user.UserDTO;
import com.example.management.user.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AppService {
    private final UserRepository userRepository;

    public AppService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AppUser getUser(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    public void addUser(AppUser user) {
        userRepository.save(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository
                .getAllUsers()
                .stream()
                .map(user -> new UserDTO(
                        user.getName(),
                        user.getUsername(),
                        user.getAuthorities()
                )).toList();
    }

    public boolean deleteUser(String user) {
        return userRepository.delete(user);
    }

    public void makeAdmin(String email) {
        var user = userRepository
                .findByUsername(email.toLowerCase())
                .orElseThrow(() -> new UserDoesNotExistException("Username not found"));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>(user.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        user.setAuthorities(authorities);

        userRepository.update(user);
    }
}
