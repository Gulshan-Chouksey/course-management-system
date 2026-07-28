package com.academiax.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.academiax.entity.User;
import com.academiax.exception.DuplicateResourceException;
import com.academiax.exception.InvalidDataException;
import com.academiax.repository.UserRepository;

/**
 * Authentication Service
 * Handles user registration, login, validation, and password operations
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Password strength regex: min 8 chars, at least 1 uppercase, 1 lowercase, 1 digit, 1 special char
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    /**
     * Register a new user
     * - Validates username and email uniqueness
     * - Validates password strength
     * - Hashes password before saving
     * 
     * @param user User object with registration details
     * @return Registered user with hashed password
     * @throws DuplicateResourceException if username or email already exists
     * @throws InvalidDataException if password is weak
     */
    public User register(User user) {
        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + user.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + user.getEmail());
        }

        // Validate password strength
        if (!isPasswordStrong(user.getPassword())) {
            throw new InvalidDataException(
                "Password must be at least 8 characters long and contain at least one uppercase letter, " +
                "one lowercase letter, one digit, and one special character (@$!%*?&)"
            );
        }

        // Hash the password before saving
        user.setPassword(hashPassword(user.getPassword()));

        // Set default values
        if (user.getCreatedDate() == null) {
            user.setCreatedDate(LocalDateTime.now());
        }
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }

        // Save and return the user
        return userRepository.save(user);
    }

    /**
     * User login - verify credentials
     * 
     * @param username User's username
     * @param password User's plain text password
     * @return User object if credentials are valid
     * @throws InvalidDataException if credentials are invalid
     */
    public User login(String username, String password) {
        // Find user by username
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new InvalidDataException("Invalid username or password");
        }

        User user = userOptional.get();

        // Check if user is active
        if (!user.getIsActive()) {
            throw new InvalidDataException("User account is inactive");
        }

        // Validate password
        if (!validateUser(username, password)) {
            throw new InvalidDataException("Invalid username or password");
        }

        return user;
    }

    /**
     * Validate user credentials
     * 
     * @param username User's username
     * @param password User's plain text password
     * @return true if credentials are valid, false otherwise
     */
    public boolean validateUser(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        // Compare the provided password with the stored hashed password
        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * Hash a password using BCrypt
     * 
     * @param password Plain text password
     * @return Hashed password
     */
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Validate password strength
     * Password must be at least 8 characters long and contain:
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character (@$!%*?&)
     * 
     * @param password Password to validate
     * @return true if password is strong, false otherwise
     */
    private boolean isPasswordStrong(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Find user by username
     * 
     * @param username User's username
     * @return Optional containing user if found
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email
     * 
     * @param email User's email
     * @return Optional containing user if found
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
