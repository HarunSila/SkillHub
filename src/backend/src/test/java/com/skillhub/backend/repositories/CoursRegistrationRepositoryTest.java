package com.skillhub.backend.repositories;

import com.skillhub.backend.models.entities.CoursRegistration;
import com.skillhub.backend.models.entities.Participant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CoursRegistrationRepositoryTest {

    @Autowired
    private CoursRegistrationRepository coursRegistrationRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    private CoursRegistration saveRegistrationWithKeycloakId(String keycloakId) {
        Participant participant = new Participant();
        participant.setKeycloakId(keycloakId);
        participantRepository.save(participant);

        CoursRegistration registration = new CoursRegistration();
        registration.setParticipant(participant);
        return coursRegistrationRepository.save(registration);
    }

    @Test
    @DisplayName("Should return registrations for given keycloakId")
    void testFindByParticipantKeycloakId_returnsMatchingRegistrations() {
        String keycloakId = "user-123";
        
        Participant participant = new Participant();
        participant.setKeycloakId(keycloakId);
        participantRepository.save(participant);

        CoursRegistration reg1 = new CoursRegistration();
        reg1.setParticipant(participant);
        coursRegistrationRepository.save(reg1);

        CoursRegistration reg2 = new CoursRegistration();
        reg2.setParticipant(participant);
        coursRegistrationRepository.save(reg2);

        saveRegistrationWithKeycloakId("other-user");

        List<CoursRegistration> result = coursRegistrationRepository.findByParticipantKeycloakId(keycloakId);

        assertThat(result)
                .hasSize(2)
                .allMatch(r -> r.getParticipant().getKeycloakId().equals(keycloakId));
    }

    @Test
    @DisplayName("Should return empty list if no registration for keycloakId")
    void testFindByParticipantKeycloakId_returnsEmptyListWhenNoMatch() {
        saveRegistrationWithKeycloakId("user-abc");

        List<CoursRegistration> result = coursRegistrationRepository.findByParticipantKeycloakId("non-existent-id");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when database is empty")
    void testFindByParticipantKeycloakId_whenDatabaseIsEmpty() {
        List<CoursRegistration> result = coursRegistrationRepository.findByParticipantKeycloakId("any-id");
        assertThat(result).isEmpty();
    }
}