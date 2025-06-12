package com.skillhub.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillhub.backend.models.TrainerStatusET;
import com.skillhub.backend.models.entities.Trainer;
import com.skillhub.backend.models.entities.UserAccount;
import com.skillhub.backend.services.ProfileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController @RequestMapping("/profile") @CrossOrigin(origins = "http://localhost:4200")
public class ProfileController {
    
    private final ProfileService profileService;

    ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/getProfiles")
    public ResponseEntity<List<UserAccount>> getProfiles() {
        try {
            List<UserAccount> profiles = this.profileService.getProfiles();
            return ResponseEntity.ok(profiles);
        } catch (Exception e) {
            System.err.println("Error fetching profiles: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/getTrainerStatus")
    public ResponseEntity<TrainerStatusET> getTrainerStatus(@RequestBody String keycloakId) {
        try {
            TrainerStatusET status = this.profileService.getTrainerStatus(keycloakId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            System.err.println("Error fetching trainer status: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/updateTrainerStatus")
    public ResponseEntity<Void> updateTrainerStatus(@RequestBody Trainer trainer) {
        try {
            if (trainer == null || trainer.getKeycloakId().isBlank()) {
                System.err.println("Invalid UserAccount data");
                return ResponseEntity.badRequest().build();
            }
            this.profileService.updateTrainerStatus(trainer);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error updating trainer status: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    @PostMapping("/saveProfile")
    public ResponseEntity<Void> saveProfile(@RequestBody UserAccount userAccount) {
        try {
            if (!isValidUserAccount(userAccount)) {
                System.err.println("Invalid user account data: " + userAccount.getKeycloakId());
                return ResponseEntity.badRequest().build();     
            }
            this.profileService.saveProfile(userAccount);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error saving profile: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/deleteProfile")
    public ResponseEntity<Void> deleteProfile(@RequestBody UserAccount userAccount) {
        try {
            if (!isValidUserAccount(userAccount)) {
                System.err.println("Invalid user account data: " + userAccount.getKeycloakId());
                return ResponseEntity.badRequest().build();     
            }
            this.profileService.deleteProfile(userAccount);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error saving profile: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    private boolean isValidUserAccount(UserAccount userAccount) {
       boolean validAccountData =
            userAccount != null && 
            isValidName(userAccount) && 
            isValidSurname(userAccount) && 
            isValidEmail(userAccount);
        if (userAccount.getNewPassword() != null && !userAccount.getNewPassword().isBlank())
            return validAccountData && isValidNewPassword(userAccount);
        else return validAccountData;
    }

    private boolean isValidName(UserAccount userAccount) {
        return  userAccount.getName() != null && !userAccount.getName().isBlank() && 
                userAccount.getName().length() <= 50 && userAccount.getName().length() >= 2;
    }

    private boolean isValidSurname(UserAccount userAccount) {
        return userAccount.getSurname() != null && !userAccount.getSurname().isBlank() && 
               userAccount.getSurname().length() <= 50 && userAccount.getSurname().length() >= 2;
    }

    private boolean isValidEmail(UserAccount userAccount) {
        return userAccount.getEmail() != null && !userAccount.getEmail().isBlank() && 
               userAccount.getEmail().length() <= 100 && userAccount.getEmail().length() >= 5 &&
               userAccount.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private boolean isValidNewPassword(UserAccount userAccount) {
        return userAccount.getNewPassword() != null && !userAccount.getNewPassword().isBlank() &&
               userAccount.getConfirmPassword() != null && !userAccount.getConfirmPassword().isBlank() &&
               userAccount.getNewPassword().equals(userAccount.getConfirmPassword()) &&
               userAccount.getNewPassword().length() >= 6 && userAccount.getNewPassword().length() <= 50;
    }
}