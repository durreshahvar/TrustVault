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
    CommandLineRunner createDevelopmentOwner(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${trustvault.dev-owner.email}") String email,
            @Value("${trustvault.dev-owner.password}") String password) {

        return args -> {
            if (password == null || password.length() < 12) {
                throw new IllegalStateException(
                    "Development owner password must be at least 12 characters."
                );
            }

            if (!userRepository.existsByEmailIgnoreCase(email)) {
                userRepository.save(new AppUser(
                    email,
                    passwordEncoder.encode(password),
                    UserRole.OWNER
                ));
                System.out.println("Created development OWNER account: " + email);
            }
        };
    }
}
