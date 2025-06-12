package com.skillhub.backend.models;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class FilterRequestDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private int maxParticipants;
    private int duration;
}
