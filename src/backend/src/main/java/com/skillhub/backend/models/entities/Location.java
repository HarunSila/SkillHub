package com.skillhub.backend.models.entities;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.skillhub.backend.models.LocationStatusT;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data @NoArgsConstructor @AllArgsConstructor
public class Location {
    
    @Id @GeneratedValue
    private UUID id;

    private String name;
    private int capacity;

    @Embedded
    private LocationStatusT status;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL ,orphanRemoval = true)
    @JsonManagedReference("location-equipment")
    private List<Equipment> equipmentList;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL ,orphanRemoval = true)
    private List<Availability> availabilityList;
}
