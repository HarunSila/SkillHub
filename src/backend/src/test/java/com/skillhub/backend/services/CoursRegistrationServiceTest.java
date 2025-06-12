package com.skillhub.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import com.skillhub.backend.models.RegistrationStatusET;
import com.skillhub.backend.models.entities.*;
import com.skillhub.backend.repositories.CoursRegistrationRepository;
import com.skillhub.backend.repositories.CoursRepository;
import com.skillhub.backend.repositories.ParticipantRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CoursRegistrationServiceTest {

    @Mock
    private CoursRepository coursRepository;
    @Mock
    private CoursRegistrationRepository coursRegistrationRepository;
    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private CoursRegistrationService coursRegistrationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        coursRegistrationService = new CoursRegistrationService(coursRepository, coursRegistrationRepository, participantRepository);
    }

    @Test
    void testRegisterCours_Success_ExistingParticipant() {
        UUID coursId = UUID.randomUUID();
        String keycloakId = "kid";
        Cours cours = mock(Cours.class);
        Participant participant = mock(Participant.class);
        CoursRegistration registration = mock(CoursRegistration.class);

        when(registration.getCours()).thenReturn(cours);
        when(registration.getParticipant()).thenReturn(participant);
        when(cours.getId()).thenReturn(coursId);
        when(coursRepository.findById(coursId)).thenReturn(Optional.of(cours));
        when(participant.getKeycloakId()).thenReturn(keycloakId);
        when(participantRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(participant));
        List<CoursRegistration> regList = new ArrayList<>();
        when(cours.getRegistrations()).thenReturn(regList);
        when(coursRepository.save(any())).thenReturn(cours);
        when(cours.getAvailabilities()).thenReturn(Collections.emptyList());

        Cours result = coursRegistrationService.registerCours(registration);

        assertNotNull(result);
        verify(participantRepository, never()).save(any());
        verify(coursRepository).save(cours);
        assertEquals(1, regList.size());
    }

    @Test
    void testRegisterCours_Success_NewParticipant() {
        UUID coursId = UUID.randomUUID();
        String keycloakId = "kid";
        Cours cours = mock(Cours.class);
        Participant participant = mock(Participant.class);
        CoursRegistration registration = mock(CoursRegistration.class);

        when(registration.getCours()).thenReturn(cours);
        when(registration.getParticipant()).thenReturn(participant);
        when(cours.getId()).thenReturn(coursId);
        when(coursRepository.findById(coursId)).thenReturn(Optional.of(cours));
        when(participant.getKeycloakId()).thenReturn(keycloakId);
        when(participantRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());
        when(participantRepository.save(participant)).thenReturn(participant);
        List<CoursRegistration> regList = new ArrayList<>();
        when(cours.getRegistrations()).thenReturn(regList);
        when(coursRepository.save(any())).thenReturn(cours);
        when(cours.getAvailabilities()).thenReturn(Collections.emptyList());

        Cours result = coursRegistrationService.registerCours(registration);

        assertNotNull(result);
        verify(participantRepository).save(participant);
        verify(coursRepository).save(cours);
        assertEquals(1, regList.size());
    }

    @Test
    void testRegisterCours_NullRegistration_Throws() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> coursRegistrationService.registerCours(null));
        assertTrue(ex.getMessage().contains("Failed"));
    }

    @Test
    void testRegisterCours_NullCours_Throws() {
        CoursRegistration registration = mock(CoursRegistration.class);
        when(registration.getCours()).thenReturn(null);
        when(registration.getParticipant()).thenReturn(mock(Participant.class));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> coursRegistrationService.registerCours(registration));
        assertTrue(ex.getMessage().contains("Failed"));
    }

    @Test
    void testRegisterCours_NullParticipant_Throws() {
        CoursRegistration registration = mock(CoursRegistration.class);
        when(registration.getCours()).thenReturn(mock(Cours.class));
        when(registration.getParticipant()).thenReturn(null);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> coursRegistrationService.registerCours(registration));
        assertTrue(ex.getMessage().contains("Failed"));
    }

    @Test
    void testRegisterCours_CourseNotFound_Throws() {
        UUID coursId = UUID.randomUUID();
        Cours cours = mock(Cours.class);
        Participant participant = mock(Participant.class);
        CoursRegistration registration = mock(CoursRegistration.class);

        when(registration.getCours()).thenReturn(cours);
        when(registration.getParticipant()).thenReturn(participant);
        when(cours.getId()).thenReturn(coursId);
        when(coursRepository.findById(coursId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> coursRegistrationService.registerCours(registration));
        assertTrue(ex.getMessage().contains("Failed"));
    }

    @Test
    void testRegisterCours_RepositoryThrowsException() {
        UUID coursId = UUID.randomUUID();
        Cours cours = mock(Cours.class);
        Participant participant = mock(Participant.class);
        CoursRegistration registration = mock(CoursRegistration.class);

        when(registration.getCours()).thenReturn(cours);
        when(registration.getParticipant()).thenReturn(participant);
        when(cours.getId()).thenReturn(coursId);
        when(coursRepository.findById(coursId)).thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> coursRegistrationService.registerCours(registration));
        assertTrue(ex.getMessage().contains("Failed to register course"));
    }

    @Test
    void testAssignRegistrationStatus_Success() {
        UUID regId = UUID.randomUUID();
        CoursRegistration registration = mock(CoursRegistration.class);
        CoursRegistration existing = mock(CoursRegistration.class);

        when(registration.getId()).thenReturn(regId);
        when(coursRegistrationRepository.findById(regId)).thenReturn(Optional.of(existing));
        when(registration.getStatus()).thenReturn(RegistrationStatusET.REGISTERED);

        assertDoesNotThrow(() -> coursRegistrationService.assignRegistrationStatus(registration));
        verify(existing).setStatus(RegistrationStatusET.REGISTERED);
        verify(coursRegistrationRepository).save(existing);
    }

    @Test
    void testAssignRegistrationStatus_NotFound_Throws() {
        UUID regId = UUID.randomUUID();
        CoursRegistration registration = mock(CoursRegistration.class);
        when(registration.getId()).thenReturn(regId);
        when(coursRegistrationRepository.findById(regId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> coursRegistrationService.assignRegistrationStatus(registration));
        assertTrue(ex.getMessage().contains("Failed"));
    }

    @Test
    void testAssignRegistrationStatus_RepositoryThrowsException() {
        UUID regId = UUID.randomUUID();
        CoursRegistration registration = mock(CoursRegistration.class);
        when(registration.getId()).thenReturn(regId);
        when(coursRegistrationRepository.findById(regId)).thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> coursRegistrationService.assignRegistrationStatus(registration));
        assertTrue(ex.getMessage().contains("Failed to assign registration status"));
    }

    @Test
    void testUnregisterCours_Success() {
        UUID regId = UUID.randomUUID();
        assertDoesNotThrow(() -> coursRegistrationService.unregisterCours(regId.toString()));
        verify(coursRegistrationRepository).deleteById(regId);
    }

    @Test
    void testUnregisterCours_RepositoryThrowsException() {
        UUID regId = UUID.randomUUID();
        doThrow(new RuntimeException("DB error")).when(coursRegistrationRepository).deleteById(regId);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> coursRegistrationService.unregisterCours(regId.toString()));
        assertTrue(ex.getMessage().contains("Failed to unregister course"));
    }
}