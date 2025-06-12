package com.skillhub.backend.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.skillhub.backend.models.RegistrationStatusET;
import com.skillhub.backend.models.entities.Availability;
import com.skillhub.backend.models.entities.Cours;
import com.skillhub.backend.models.entities.CoursRegistration;
import com.skillhub.backend.models.entities.Location;
import com.skillhub.backend.models.entities.Participant;
import com.skillhub.backend.models.entities.Trainer;

@DataJpaTest
class CoursRepositoryTest {
    
    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private CoursRegistrationRepository coursRegistrationRepository;

    private Cours createCoursWithAvailabilityAndLocation(LocalDate endDate, String trainerKeycloakId) {
        Cours cours = new Cours();
        cours.setEndDate(endDate);
        cours.setMaxParticipants(10);

        Trainer trainer = new Trainer();
        trainer.setKeycloakId(trainerKeycloakId);
        trainerRepository.save(trainer);

        Location location = new Location();
        location.setName("Test Location");
        locationRepository.save(location);

        Availability availability = new Availability();
        availability.setTrainer(trainer);
        availability.setLocation(location);
        availability.setCours(cours);

        cours.setAvailabilities(new ArrayList<>(List.of(availability)));
        coursRepository.save(cours);
        availabilityRepository.save(availability);

        return cours;
    }

    @Test
    @DisplayName("Should find all courses with availabilities and not ended")
    void testFindAllWithAvailabilitiesAndNotEnded() {
        createCoursWithAvailabilityAndLocation(LocalDate.now().plusDays(1), "trainer-1");
        createCoursWithAvailabilityAndLocation(LocalDate.now().minusDays(1), "trainer-2");

        List<Cours> result = coursRepository.findAllWithAvailabilitiesAndNotEnded();
        assertThat(result).allMatch(c -> !c.getEndDate().isBefore(LocalDate.now()));
    }

    @Test
    @DisplayName("Should find course by id with availabilities and location")
    void testFindById() {
        Cours cours = createCoursWithAvailabilityAndLocation(LocalDate.now().plusDays(1), "trainer-1");
        Optional<Cours> found = coursRepository.findById(cours.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getAvailabilities()).isNotEmpty();
    }

    @Test
    @DisplayName("Should find all courses by trainer keycloakId")
    void testFindAllWithAvailabilitiesByKeycloakId() {
        createCoursWithAvailabilityAndLocation(LocalDate.now().plusDays(1), "trainer-1");
        createCoursWithAvailabilityAndLocation(LocalDate.now().plusDays(1), "trainer-2");

        List<Cours> result = coursRepository.findAllWithAvailabilitiesByKeycloakId("trainer-1");
        assertThat(result).isNotEmpty();
        assertThat(result.stream().flatMap(c -> c.getAvailabilities().stream())
                .allMatch(a -> a.getTrainer().getKeycloakId().equals("trainer-1"))).isTrue();
    }

    @Test
    @DisplayName("Should find all unregistered courses with availabilities and not ended")
    void testFindAllUnregisteredWithAvailabilitiesAndNotEnded() {
        String keycloakId = "participant-1";
        Participant participant = new Participant();
        participant.setKeycloakId(keycloakId);
        participantRepository.save(participant);

        Cours cours = createCoursWithAvailabilityAndLocation(LocalDate.now().plusDays(1), "trainer-1");
        coursRepository.save(cours);

        // Register participant to another course
        Cours otherCours = createCoursWithAvailabilityAndLocation(LocalDate.now().plusDays(1), "trainer-2");
        coursRepository.save(otherCours);

        CoursRegistration registration = new CoursRegistration();
        registration.setParticipant(participant);
        registration.setCours(otherCours);
        registration.setStatus(RegistrationStatusET.REGISTERED);
        coursRegistrationRepository.save(registration);

        List<Cours> result = coursRepository.findAllUnregisteredWithAvailabilitiesAndNotEnded(keycloakId, RegistrationStatusET.REGISTERED);
        assertThat(result).anyMatch(c -> c.getId().equals(cours.getId()));
    }

    @Test
    @DisplayName("Should find registered courses by participant keycloakId")
    void testFindRegisteredCoursesByKeycloakId() {
        String keycloakId = "participant-1";
        Participant participant = new Participant();
        participant.setKeycloakId(keycloakId);
        participantRepository.save(participant);

        Cours cours = createCoursWithAvailabilityAndLocation(LocalDate.now().plusDays(1), "trainer-1");
        coursRepository.save(cours);

        CoursRegistration registration = new CoursRegistration();
        registration.setParticipant(participant);
        registration.setCours(cours);
        registration.setStatus(RegistrationStatusET.REGISTERED);
        coursRegistrationRepository.save(registration);

        List<Cours> result = coursRepository.findRegisteredCoursesByKeycloakId(keycloakId);
        assertThat(result).anyMatch(c -> c.getId().equals(cours.getId()));
    }
}