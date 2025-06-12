package com.skillhub.backend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillhub.backend.models.TrainerStatusET;
import com.skillhub.backend.models.entities.Trainer;

public interface TrainerRepository extends JpaRepository<Trainer, UUID> {

    @Query("SELECT t.status FROM Trainer t WHERE t.keycloakId = :keycloakId")
    Optional<TrainerStatusET> getTrainerStatusByKeycloakId(@Param("keycloakId") String keycloakId);

    @Query("SELECT t FROM Trainer t WHERE t.keycloakId = :keycloakId")
    Optional<Trainer> findByKeycloakId(@Param("keycloakId") String keycloakId);
}
