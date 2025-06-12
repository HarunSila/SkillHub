package com.skillhub.backend.models.entities;

import java.time.LocalTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.skillhub.backend.models.DayET;

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
public class Availability {
    @Id @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    @JsonIgnoreProperties("availabilities")
    private Trainer trainer;

    @ManyToOne
    @JoinColumn(name = "cours_id")
    @JsonBackReference("cours-availability")
    private Cours cours;

    @ManyToOne
    @JoinColumn(name = "location_id")
    @JsonIgnoreProperties({"availabilityList", "equipmentList"})
    private Location location;
    
    @Enumerated(EnumType.STRING)
    private DayET weekday;

    private LocalTime startTime;
    private LocalTime endTime;
}
