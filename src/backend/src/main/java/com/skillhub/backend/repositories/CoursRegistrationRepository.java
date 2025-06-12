package com.skillhub.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.skillhub.backend.models.entities.CoursRegistration;

@Repository
public interface CoursRegistrationRepository extends JpaRepository<CoursRegistration, UUID> {

    @Query("SELECT cr FROM CoursRegistration cr WHERE cr.participant.keycloakId = :keycloakId")
    List<CoursRegistration> findByParticipantKeycloakId(String keycloakId);
}
