package com.example.evbatteryhealth.controller;

import com.example.evbatteryhealth.model.User;
import com.example.evbatteryhealth.model.PassportLink;
import com.example.evbatteryhealth.model.BatteryAnalysis;
import com.example.evbatteryhealth.repository.UserRepository;
import com.example.evbatteryhealth.repository.PassportLinkRepository;
import com.example.evbatteryhealth.repository.BatteryAnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/marketplace/passport-links")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:3000",
        "http://127.0.0.1:5173",
        "http://127.0.0.1:3000",
        "https://evcharging-1.onrender.com"
})
public class PassportLinkController {

    private final UserRepository userRepository;
    private final PassportLinkRepository passportLinkRepository;
    private final BatteryAnalysisRepository batteryAnalysisRepository;

    @Autowired
    public PassportLinkController(UserRepository userRepository,
                                  PassportLinkRepository passportLinkRepository,
                                  BatteryAnalysisRepository batteryAnalysisRepository) {
        this.userRepository = userRepository;
        this.passportLinkRepository = passportLinkRepository;
        this.batteryAnalysisRepository = batteryAnalysisRepository;
    }

    @PostMapping
    public ResponseEntity<?> linkPassport(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        String vehicleId = request.get("vehicleId");

        if (phoneNumber == null || phoneNumber.trim().isEmpty() ||
            vehicleId == null || vehicleId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Phone number and Vehicle ID are required."));
        }

        phoneNumber = phoneNumber.trim();
        vehicleId = vehicleId.trim();

        // Get the latest BatteryAnalysis for this vehicleId
        List<BatteryAnalysis> analyses = batteryAnalysisRepository.findByVehicleVehicleIdOrderByCreatedAtDesc(vehicleId);
        if (analyses.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "No battery passport exists for this Vehicle ID. Please generate a diagnostic passport first."));
        }

        BatteryAnalysis latestAnalysis = analyses.get(0);

        // Check if there is already a link for this phone number, and overwrite or update it
        PassportLink link = passportLinkRepository.findByPhoneNumber(phoneNumber)
                .orElse(new PassportLink());
        
        link.setPhoneNumber(phoneNumber);
        link.setVehicleId(vehicleId);
        link.setAssessmentId(latestAnalysis.getId());
        passportLinkRepository.save(link);

        return ResponseEntity.ok(Map.of("success", true, "message", "Phone number successfully linked to Vehicle ID " + vehicleId + " and battery passport."));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtpAndGetPassport(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Phone number is required."));
        }

        phoneNumber = phoneNumber.trim();

        // Look up PassportLink
        Optional<PassportLink> optLink = passportLinkRepository.findByPhoneNumber(phoneNumber);
        if (optLink.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "No passport link found for this phone number."));
        }

        PassportLink link = optLink.get();

        // Fetch battery analysis and history logs
        Optional<BatteryAnalysis> optAnalysis = batteryAnalysisRepository.findById(link.getAssessmentId());
        if (optAnalysis.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Linked battery passport could not be found."));
        }

        BatteryAnalysis assessment = optAnalysis.get();
        List<BatteryAnalysis> historyLogs = batteryAnalysisRepository.findByVehicleVehicleIdOrderByCreatedAtAsc(link.getVehicleId());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "assessment", assessment,
            "history", historyLogs
        ));
    }
}
