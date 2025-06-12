package com.skillhub.backend.models.entities;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data @NoArgsConstructor @AllArgsConstructor
public class Cours {
    
    @Id @GeneratedValue
    private UUID id;

    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private int maxParticipants;
    private List<String> pictureUrls;

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("cours-availability")
    private List<Availability> availabilities;

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("cours-registration")
    private List<CoursRegistration> registrations;
}