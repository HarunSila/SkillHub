package com.skillhub.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.skillhub.backend.models.RegistrationStatusET;
import com.skillhub.backend.models.entities.Cours;

@Repository
public interface CoursRepository extends JpaRepository<Cours, UUID> {

      @Query("SELECT DISTINCT c FROM Cours c " +
            "LEFT JOIN FETCH c.availabilities a " +
            "LEFT JOIN FETCH a.location " +
            "WHERE c.endDate >= CURRENT_DATE")
      List<Cours> findAllWithAvailabilitiesAndNotEnded();

      @Query("SELECT c FROM Cours c " +
            "LEFT JOIN FETCH c.availabilities a " +
            "LEFT JOIN FETCH a.location " +
            "WHERE c.id = :id")
      Optional<Cours> findById(UUID id);

      @Query("SELECT c FROM Cours c " +
            "LEFT JOIN FETCH c.availabilities a " +
            "LEFT JOIN FETCH a.location " +
            "WHERE a.trainer.keycloakId = :keycloakId")
      List<Cours> findAllWithAvailabilitiesByKeycloakId(String keycloakId);

      @Query("SELECT DISTINCT c FROM Cours c " +
            "LEFT JOIN FETCH c.availabilities a " +
            "LEFT JOIN FETCH a.location " +
            "WHERE c.endDate >= CURRENT_DATE " +
            "AND NOT EXISTS (" +
            "  SELECT r FROM c.registrations r " +
            "  WHERE r.participant.keycloakId = :keycloakId" +
            ") " +
            "AND (" +
            "  SELECT COUNT(r) FROM c.registrations r " +
            "  WHERE r.status = :status" +
            ") < c.maxParticipants")
      List<Cours> findAllUnregisteredWithAvailabilitiesAndNotEnded(String keycloakId, RegistrationStatusET status);

      @Query("SELECT c FROM Cours c " +
            "LEFT JOIN FETCH c.availabilities a " +
            "LEFT JOIN FETCH a.location " +
            "WHERE c.id IN (" +
            "  SELECT r.cours.id FROM c.registrations r " +
            "  WHERE r.participant.keycloakId = :keycloakId" +
            ")")
      List<Cours> findRegisteredCoursesByKeycloakId(String keycloakId);
}
