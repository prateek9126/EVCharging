package com.example.evbatteryhealth.controller;

import com.example.evbatteryhealth.model.User;
import com.example.evbatteryhealth.repository.UserRepository;
import com.example.evbatteryhealth.util.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000"})
public class AuthController {

    private final UserRepository userRepository;

    @Autowired
    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String phoneNumber = request.get("phoneNumber");
        String gmail = request.get("gmail");
        String password = request.get("password");

        if (name == null || name.trim().isEmpty() ||
            gmail == null || gmail.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "All fields are required."));
        }

        gmail = gmail.trim().toLowerCase();

        // Verify user doesn't exist
        if (userRepository.findByGmail(gmail).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Gmail is already registered."));
        }

        // Hash password
        String passwordHash = PasswordEncoder.hashPassword(password);

        // Save User
        User user = new User(name, phoneNumber, gmail, passwordHash);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "Registration successful. You can now log in."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String gmail = request.get("gmail");
        String password = request.get("password");

        if (gmail == null || gmail.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Gmail and password are required."));
        }

        gmail = gmail.trim().toLowerCase();

        // Find user
        Optional<User> optUser = userRepository.findByGmail(gmail);
        if (optUser.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid email or password."));
        }

        User user = optUser.get();
        if (!PasswordEncoder.checkPassword(password, user.getPasswordHash())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid email or password."));
        }

        return ResponseEntity.ok(Map.of(
            "success", true,
            "name", user.getName(),
            "gmail", user.getGmail()
        ));
    }
}
