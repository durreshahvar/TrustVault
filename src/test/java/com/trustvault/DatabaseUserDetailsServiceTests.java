package com.trustvault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.trustvault.security.DatabaseUserDetailsService;
import com.trustvault.user.AppUser;
import com.trustvault.user.AppUserRepository;
import com.trustvault.user.UserRole;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class DatabaseUserDetailsServiceTests {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private DatabaseUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;
@Test
void loadsStoredUserWithCorrectRole() {
    String email = "role-test@example.test";
    String password = "Example-Only-Password-123!";
    String hash = passwordEncoder.encode(password);

    userRepository.save(new AppUser(email, hash, UserRole.OWNER));

    UserDetails details =
        userDetailsService.loadUserByUsername(email.toUpperCase());

    assertEquals(email, details.getUsername());
    assertTrue(details.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_OWNER")));
    assertTrue(passwordEncoder.matches(password, details.getPassword()));
}

    @Test
    void unknownEmailIsRejected() {
        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername("missing@example.test")
        );
    }
}
