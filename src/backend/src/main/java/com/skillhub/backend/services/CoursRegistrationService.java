package com.skillhub.backend.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.skillhub.backend.models.entities.Cours;
import com.skillhub.backend.models.entities.CoursRegistration;
import com.skillhub.backend.models.entities.Participant;
import com.skillhub.backend.models.entities.Trainer;
import com.skillhub.backend.repositories.CoursRegistrationRepository;
import com.skillhub.backend.repositories.CoursRepository;
import com.skillhub.backend.repositories.ParticipantRepository;

@Service
public class CoursRegistrationService {
    
    private final CoursRepository coursRepository;
    private final CoursRegistrationRepository coursRegistrationRepository;
    private final ParticipantRepository participantRepository;

    CoursRegistrationService(
        CoursRepository coursRepository, 
        CoursRegistrationRepository coursRegistrationRepository,
        ParticipantRepository participantRepository
    ) {
        this.coursRepository = coursRepository;
        this.coursRegistrationRepository = coursRegistrationRepository;
        this.participantRepository = participantRepository;
    }

    public Cours registerCours(CoursRegistration registration) {
        try {
            if (registration == null || registration.getCours() == null || registration.getParticipant() == null) {
                throw new IllegalArgumentException("Registration, Cours, and Participant must not be null");
            }

            Cours cours = coursRepository.findById(registration.getCours().getId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + registration.getCours().getId()));
            cours = this.fetchTrainerData(cours);

            Participant participant = participantRepository.findByKeycloakId(registration.getParticipant().getKeycloakId())
                .orElse(null);
            if (participant == null) {
                participant = this.participantRepository.save(registration.getParticipant());
            }

            registration.setParticipant(participant);

            List<CoursRegistration> registrations = cours.getRegistrations();
            registrations.add(registration);
            cours.setRegistrations(registrations);    
            
            return coursRepository.save(cours);
        } catch (Exception e) {
            System.err.println("Error registering course: " + e.getMessage());
            throw new RuntimeException("Failed to register course", e);
        } 
    }

    public void assignRegistrationStatus(CoursRegistration registration) {
        try {
            CoursRegistration existingRegistration = coursRegistrationRepository.findById(registration.getId())
                .orElseThrow(() -> new IllegalArgumentException("Registration not found with ID: " + registration.getId()));

            existingRegistration.setStatus(registration.getStatus());
            coursRegistrationRepository.save(existingRegistration);
        } catch (Exception e) {
            System.err.println("Error assigning registration status: " + e.getMessage());
            throw new RuntimeException("Failed to assign registration status", e);
        }
    }

    public void unregisterCours(String registrationId) {
        try {
            this.coursRegistrationRepository.deleteById(UUID.fromString(registrationId));
        } catch (Exception e) {
            System.err.println("Error unregistering course: " + e.getMessage());
            throw new RuntimeException("Failed to unregister course", e);
        }
    }

    private Cours fetchTrainerData(Cours cours) {
        cours.getAvailabilities().forEach(availability -> {
            Trainer trainer = availability.getTrainer();
            trainer.setRole("trainer");
            availability.setTrainer(trainer);
        });
        return cours;
    }
}