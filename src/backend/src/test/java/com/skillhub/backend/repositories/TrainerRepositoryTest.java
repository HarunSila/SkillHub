package com.skillhub.backend.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.skillhub.backend.models.TrainerStatusET;
import com.skillhub.backend.models.entities.Trainer;

@DataJpaTest
class TrainerRepositoryTest {
    
    @Autowired
    private TrainerRepository trainerRepository;

    @Test
    @DisplayName("Should find trainer status by keycloakId")
    void testGetTrainerStatusByKeycloakId() {
        String keycloakId = "trainer-123";
        Trainer trainer = new Trainer();
        trainer.setKeycloakId(keycloakId);
        trainer.setStatus(TrainerStatusET.AKTIV);
        trainerRepository.save(trainer);

        TrainerStatusET status = trainerRepository.getTrainerStatusByKeycloakId(keycloakId).orElse(null);
        
        assertNotNull(status, "Trainer status should not be null");
        assertEquals(TrainerStatusET.AKTIV, status, "Trainer status should be AKTIV");
    }

    @Test
    @DisplayName("Should return empty Optional if no trainer found by keycloakId for status")   
    void testGetTrainerStatusByKeycloakId_NotFound() {
        String keycloakId = "non-existent-trainer";

        TrainerStatusET status = trainerRepository.getTrainerStatusByKeycloakId(keycloakId).orElse(null);
        
        assertEquals(null, status, "Trainer status should be null for non-existent keycloakId");
    }

    @Test
    @DisplayName("Should find trainer by keycloakId")
    void testFindByKeycloakId() {
        String keycloakId = "trainer-456";
        Trainer trainer = new Trainer();
        trainer.setKeycloakId(keycloakId);
        trainerRepository.save(trainer);

        Trainer foundTrainer = trainerRepository.findByKeycloakId(keycloakId).orElse(null);
        
        assertNotNull(foundTrainer, "Trainer should not be null");
        assertEquals(keycloakId, foundTrainer.getKeycloakId(), "Trainer keycloakId should match");
    }

    @Test
    @DisplayName("Should return empty Optional if no trainer found by keycloakId")
    void testFindByKeycloakId_NotFound() {
        String keycloakId = "non-existent-trainer";

        Trainer foundTrainer = trainerRepository.findByKeycloakId(keycloakId).orElse(null);
        
        assertEquals(null, foundTrainer, "Trainer should be null for non-existent keycloakId");
    }
}