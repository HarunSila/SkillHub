package com.skillhub.backend.models.entities;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.skillhub.backend.models.RegistrationStatusET;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data @NoArgsConstructor @AllArgsConstructor
public class CoursRegistration {
    
    @Id @GeneratedValue
    private UUID id;
    
    private LocalDate registrationDate;

    @Enumerated(EnumType.STRING)
    private RegistrationStatusET status;

    @ManyToOne
    @JoinColumn(name = "participant_id")
    @JsonIgnoreProperties("coursRegistrations")
    private Participant participant;

    @ManyToOne
    @JoinColumn(name = "cours_id")
    @JsonBackReference("cours-registration")
    private Cours cours;
}
