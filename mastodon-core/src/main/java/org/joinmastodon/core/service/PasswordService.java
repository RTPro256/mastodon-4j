package org.joinmastodon.core.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String hashed) {
        if (rawPassword == null || hashed == null) {
            return false;
        }
        if (!hashed.startsWith("$2")) {
            return rawPassword.equals(hashed);
        }
        return encoder.matches(rawPassword, hashed);
    }
}
