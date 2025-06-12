package com.skillhub.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillhub.backend.models.entities.Cours;
import com.skillhub.backend.models.entities.CoursRegistration;
import com.skillhub.backend.services.CoursRegistrationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController @RequestMapping("/cours-registration") @CrossOrigin(origins = "http://localhost:4200")
public class CoursRegistrationController {
    
    private final CoursRegistrationService coursRegistrationService;

    CoursRegistrationController(CoursRegistrationService coursRegistrationService) {
        this.coursRegistrationService = coursRegistrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<Cours> registerCours(@RequestBody CoursRegistration registration) {
        try {
            Cours registeredCours = coursRegistrationService.registerCours(registration);
            return ResponseEntity.ok(registeredCours);
        } catch (Exception e) {
            System.err.println("Error registering cours: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/assign-status")
    public ResponseEntity<Void> assignRegistrationStatus(@RequestBody CoursRegistration registration) {
        try {
            this.coursRegistrationService.assignRegistrationStatus(registration);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error assigning status: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/unregister/{id}")
    public ResponseEntity<Void> unregisterCours(@PathVariable String id) {
        try {
            this.coursRegistrationService.unregisterCours(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error unregistering course: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
