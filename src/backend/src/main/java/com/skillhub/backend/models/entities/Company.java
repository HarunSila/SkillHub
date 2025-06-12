package com.skillhub.backend.models.entities;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.skillhub.backend.models.AddressT;
import com.skillhub.backend.models.OpeningTimeT;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data @NoArgsConstructor @AllArgsConstructor
public class Company {
    @Id @GeneratedValue
    private UUID id;

    private String name;
    private String contactEmail;
    private String contactPhone;
    private LocalDate registrationDate;

    @Embedded
    private AddressT address;
    
    @ElementCollection
    @CollectionTable(name = "company_opening_times", joinColumns = @JoinColumn(name = "company_id"))
    private List<OpeningTimeT> openingTimes;
}
