package com.skillhub.backend.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.skillhub.backend.models.entities.Participant;

@DataJpaTest
class ParticipantRepositoryTest {
    
    @Autowired
    private ParticipantRepository participantRepository;

    @Test
    @DisplayName("Should find participant by keycloakId")
    void testFindByKeycloakId() {
        String keycloakId = "test-user-123";
        
        Participant participant = new Participant();
        participant.setKeycloakId(keycloakId);
        participantRepository.save(participant);

        Optional<Participant> foundParticipant = participantRepository.findByKeycloakId(keycloakId);
        
        assertTrue(foundParticipant.isPresent());
        assertEquals(keycloakId, foundParticipant.get().getKeycloakId());
    }

    @Test
    @DisplayName("Should return empty Optional if no participant found by keycloakId")
    void testFindByKeycloakId_NotFound() {
        String keycloakId = "non-existent-user";

        Optional<Participant> foundParticipant = participantRepository.findByKeycloakId(keycloakId);
        
        assertTrue(foundParticipant.isEmpty());
    }
}
