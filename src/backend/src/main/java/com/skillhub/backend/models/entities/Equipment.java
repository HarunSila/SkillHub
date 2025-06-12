package com.skillhub.backend.models.entities;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data @NoArgsConstructor @AllArgsConstructor
public class Equipment {
    
    @Id @GeneratedValue
    private UUID id;

    private String name;
    private String description;
    private int amount;

    @ManyToOne
    @JoinColumn(name = "location_id")
    @JsonBackReference("location-equipment")
    private Location location;
}
