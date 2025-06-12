package com.skillhub.backend.models;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class TimeRangeDTO {
    private LocalTime startTime;
    private LocalTime endTime;
}
