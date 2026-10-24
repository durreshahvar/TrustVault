package com.trustvault;

import static org.junit.jupiter.api.Assertions.*;

import com.trustvault.user.AppUser;
import com.trustvault.user.AppUserRepository;
import com.trustvault.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserRepositoryTests {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
void savedUserCanBeFoundByEmailAndPasswordIsHashed() {
    String email = "repository-test@example.test";
    String rawPassword = "Example-Only-Password-123!";
    String hash = passwordEncoder.encode(rawPassword);

    AppUser user = new AppUser(email, hash, UserRole.OWNER);
    userRepository.save(user);

    AppUser found = userRepository
        .findByEmailIgnoreCase("REPOSITORY-TEST@example.test")
        .orElseThrow();

    assertEquals(UserRole.OWNER, found.getRole());
    assertNotEquals(rawPassword, found.getPasswordHash());
    assertTrue(passwordEncoder.matches(rawPassword, found.getPasswordHash()));
    assertFalse(passwordEncoder.matches(
        "Wrong-Password", found.getPasswordHash()
    ));
}
}
