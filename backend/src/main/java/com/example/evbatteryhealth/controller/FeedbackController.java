package com.example.evbatteryhealth.controller;

import com.example.evbatteryhealth.model.Feedback;
import com.example.evbatteryhealth.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:3000",
        "http://127.0.0.1:5173",
        "http://127.0.0.1:3000",
        "https://evcharging-1.onrender.com"
})
public class FeedbackController {

    private final FeedbackService service;

    @Autowired
    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> createFeedback(@Valid @RequestBody Feedback feedback) {
        try {
            if (feedback.getRating() == null || feedback.getRating() < 1 || feedback.getRating() > 5) {
                return ResponseEntity.badRequest().body(java.util.Map.of("message", "Rating must be between 1 and 5."));
            }
            if (feedback.getFeedback() == null || feedback.getFeedback().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(java.util.Map.of("message", "Feedback message cannot be empty."));
            }
            feedback.setFeedback(feedback.getFeedback().trim());
            Feedback saved = service.save(feedback);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of("message", "Internal server error occurred."));
        }
    }

    @GetMapping
    public ResponseEntity<List<Feedback>> getFeedback() {
        return ResponseEntity.ok(service.getAllFeedback());
    }
}
