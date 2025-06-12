package com.skillhub.backend.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Embeddable
public class LocationStatusT {
    private boolean active;
    private String description;
}
