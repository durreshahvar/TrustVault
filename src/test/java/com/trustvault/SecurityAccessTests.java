package com.trustvault;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class SecurityAccessTests {

    @Test
    void passwordEncoderRejectsWrongPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("Correct-Example-Password-123!");

        assertTrue(encoder.matches(
            "Wrong-Password",
            hash
        ) == false);
    }
}
