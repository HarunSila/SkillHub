package com.skillhub.backend.models;

import java.time.LocalTime;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Embeddable
public class OpeningTimeT {

    @Enumerated(EnumType.STRING)
    private DayET weekday;

    private LocalTime startTime;
    private LocalTime endTime;
}