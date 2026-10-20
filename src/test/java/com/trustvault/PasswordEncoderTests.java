package com.trustvault;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordEncoderTests {

    @Test
    void passwordIsHashedAndCanBeVerified() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "Example-Only-Password-123!";
        String hash = encoder.encode(rawPassword);

        assertNotEquals(rawPassword, hash);
        assertTrue(encoder.matches(rawPassword, hash));
        assertFalse(encoder.matches("Wrong-Password", hash));
    }
}
