package com.trustvault.user;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AppUserRepository userRepository;

    public UserController(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public record UserResponse(String email) {}

    @GetMapping
    public List<UserResponse> listUsers(Authentication authentication) {
        String currentEmail = authentication.getName();

        return userRepository.findAll().stream()
                .filter(user -> !user.getEmail().equalsIgnoreCase(currentEmail))
                .map(user -> new UserResponse(user.getEmail()))
                .toList();
    }
}
