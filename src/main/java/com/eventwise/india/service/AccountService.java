package com.eventtoimpact.india.service;

import com.eventtoimpact.india.model.UserAccount;
import com.eventtoimpact.india.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AccountService {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-z0-9._-]{3,30}");

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserAccountRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccount register(String username, String password) {
        String normalizedUsername = normalize(username);
        validate(normalizedUsername, password);
        if (repository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new IllegalArgumentException("That username is already registered. Choose another one.");
        }

        UserAccount account = new UserAccount();
        account.setUsername(normalizedUsername);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setCreatedAt(LocalDateTime.now());
        return repository.save(account);
    }

    public boolean exists(String username) {
        return repository.existsByUsernameIgnoreCase(normalize(username));
    }

    private void validate(String username, String password) {
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(
                    "Username must be 3–30 characters using letters, numbers, dot, underscore or hyphen.");
        }
        if (password == null || password.length() < 6 || password.length() > 72) {
            throw new IllegalArgumentException("Password must contain between 6 and 72 characters.");
        }
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
