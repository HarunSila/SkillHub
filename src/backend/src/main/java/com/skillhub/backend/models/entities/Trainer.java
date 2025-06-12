package com.skillhub.backend.models.entities;

import java.util.List;

import com.skillhub.backend.models.TrainerStatusET;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity @Data @NoArgsConstructor @AllArgsConstructor
@DiscriminatorValue("TRAINER") @EqualsAndHashCode(callSuper = true)
public class Trainer extends UserAccount {
    @Enumerated(EnumType.STRING)
    private TrainerStatusET status;

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Availability> availabilities;
}
