package com.skillhub.backend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.skillhub.backend.models.entities.Participant;

public interface ParticipantRepository extends JpaRepository<Participant, UUID> {

    @Query("SELECT p FROM Participant p WHERE p.keycloakId = :keycloakId")
    Optional<Participant> findByKeycloakId(String keycloakId);
}
