package com.in6225.fooddelivery.service;

import com.in6225.fooddelivery.entity.Role;
import com.in6225.fooddelivery.entity.User;
import com.in6225.fooddelivery.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String name, String email, String password, String phone, String address, Role role) {
        if (name == null || name.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Name and email are required.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        if (role == Role.ADMIN) {
            throw new IllegalArgumentException("You cannot register as an admin.");
        }
        String cleanEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        User user = new User(name.trim(), cleanEmail, passwordEncoder.encode(password), phone, address, role);
        return userRepository.save(user);
    }

    /** Returns the user if the email + password match, otherwise empty. */
    public Optional<User> login(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.trim().toLowerCase())
                .filter(user -> passwordEncoder.matches(password, user.getPassword()));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
