
package com.trustvault.security;

import com.trustvault.user.AppUser;
import com.trustvault.user.AppUserRepository;
import com.trustvault.user.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DevDataInitializer {

    @Bean
    CommandLineRunner createDevelopmentUsers(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${trustvault.dev-owner.email:}") String ownerEmail,
            @Value("${trustvault.dev-owner.password:}") String ownerPassword,
            @Value("${trustvault.dev-viewer.email:}") String viewerEmail,
            @Value("${trustvault.dev-viewer.password:}") String viewerPassword) {

        return args -> {
            createUserIfMissing(
                    userRepository, passwordEncoder,
                    ownerEmail, ownerPassword, UserRole.OWNER);

            createUserIfMissing(
                    userRepository, passwordEncoder,
                    viewerEmail, viewerPassword, UserRole.VIEWER);
        };
    }

    private void createUserIfMissing(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String email,
            String password,
            UserRole role) {

        if (email == null || email.isBlank()
                || password == null || password.length() < 12) {
            throw new IllegalStateException(
                    "Development " + role
                    + " email must be set and password must be at least 12 characters.");
        }
var existingUser = userRepository.findByEmailIgnoreCase(email.trim());

if (existingUser.isEmpty()) {
    userRepository.save(new AppUser(
            email.trim(),
            passwordEncoder.encode(password),
            role));
    System.out.println(
            "Created development " + role + " account: " + email);
} else if (role == UserRole.OWNER) {
    AppUser owner = existingUser.get();
    owner.updatePasswordHash(passwordEncoder.encode(password));
    userRepository.save(owner);
    System.out.println("Updated local development owner password.");
}
    }
}