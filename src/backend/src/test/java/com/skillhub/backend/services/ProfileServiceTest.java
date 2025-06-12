package com.skillhub.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.json.JSONObject;
import org.json.JSONArray;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.skillhub.backend.models.TrainerStatusET;
import com.skillhub.backend.models.entities.*;
import com.skillhub.backend.repositories.*;
import java.util.*;

class ProfileServiceTest {
    
    @Mock
    TrainerRepository trainerRepository;
    @Mock
    ParticipantRepository participantRepository;
    @Mock
    CoursRegistrationRepository coursRegistrationRepository;
    @Mock
    CoursRepository coursRepository;
    @Mock
    AvailabilityRepository availabilityRepository;

    @InjectMocks
    ProfileService profileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        profileService = Mockito.spy(new ProfileService(
            trainerRepository, participantRepository, coursRegistrationRepository, coursRepository, availabilityRepository
        ));
    }

    @Test
    void testGetUserPublicInfo_Trainer() {
        Trainer trainer = new Trainer();
        trainer.setKeycloakId("kid");
        JSONObject userJson = new JSONObject();
        userJson.put("firstName", "John");
        userJson.put("lastName", "Doe");
        userJson.put("username", "johndoe");
        userJson.put("email", "john@doe.com");

        doNothing().when(profileService).initWebClient();
        doReturn(userJson).when(profileService).getUserFromKeycloak("kid");

        UserAccount result = profileService.getUserPublicInfo(trainer);

        assertEquals("John", result.getName());
        assertEquals("Doe", result.getSurname());
        assertEquals("johndoe", result.getUsername());
        assertEquals("john@doe.com", result.getEmail());
    }

    @Test
    void testGetUserPublicInfo_Participant() {
        Participant participant = new Participant();
        participant.setKeycloakId("pid");
        JSONObject userJson = new JSONObject();
        userJson.put("firstName", "Jane");
        userJson.put("lastName", "Smith");
        userJson.put("username", "janesmith");
        userJson.put("email", "jane@smith.com");

        doNothing().when(profileService).initWebClient();
        doReturn(userJson).when(profileService).getUserFromKeycloak("pid");

        UserAccount result = profileService.getUserPublicInfo(participant);

        assertEquals("janesmith", result.getUsername());
        assertEquals("jane@smith.com", result.getEmail());
    }

    @Test
    void testGetProfiles_FiltersAdminAndMapsRoles() {
        doNothing().when(profileService).initWebClient();

        JSONArray users = new JSONArray();
        JSONObject trainerUser = new JSONObject();
        trainerUser.put("id", "tid");
        trainerUser.put("username", "trainer1");
        trainerUser.put("firstName", "T");
        trainerUser.put("lastName", "Rainer");
        trainerUser.put("email", "t@r.com");
        users.put(trainerUser);

        JSONObject participantUser = new JSONObject();
        participantUser.put("id", "pid");
        participantUser.put("username", "participant1");
        participantUser.put("firstName", "P");
        participantUser.put("lastName", "Articipant");
        participantUser.put("email", "p@a.com");
        users.put(participantUser);

        JSONObject adminUser = new JSONObject();
        adminUser.put("id", "aid");
        adminUser.put("username", "admin1");
        adminUser.put("firstName", "A");
        adminUser.put("lastName", "Dmin");
        adminUser.put("email", "a@d.com");
        users.put(adminUser);

        List<JSONObject> usersWithRoles = new ArrayList<>();
        JSONObject trainerWithRole = new JSONObject(trainerUser.toString());
        trainerWithRole.put("clientRoles", Arrays.asList("trainer"));
        usersWithRoles.add(trainerWithRole);

        JSONObject participantWithRole = new JSONObject(participantUser.toString());
        participantWithRole.put("clientRoles", Arrays.asList("participant"));
        usersWithRoles.add(participantWithRole);

        JSONObject adminWithRole = new JSONObject(adminUser.toString());
        adminWithRole.put("clientRoles", Arrays.asList("admin"));
        usersWithRoles.add(adminWithRole);

        doReturn(users).when(profileService).getUsersFromKeycloak();
        doReturn(usersWithRoles).when(profileService).getRolesFromKeycloak(any(), any());

        when(trainerRepository.getTrainerStatusByKeycloakId("tid")).thenReturn(Optional.of(TrainerStatusET.AKTIV));

        List<UserAccount> result = profileService.getProfiles();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u instanceof Trainer && "trainer".equals(u.getRole())));
        assertTrue(result.stream().anyMatch(u -> u instanceof Participant && "participant".equals(u.getRole())));
    }

    @Test
    void testGetTrainerStatus() {
        when(trainerRepository.getTrainerStatusByKeycloakId("tid")).thenReturn(Optional.of(TrainerStatusET.AKTIV));
        TrainerStatusET status = profileService.getTrainerStatus("tid");
        assertEquals(TrainerStatusET.AKTIV, status);
    }

    @Test
    void testUpdateTrainerStatus_ExistingTrainer() {
        Trainer trainer = new Trainer();
        trainer.setKeycloakId("tid");
        trainer.setStatus(TrainerStatusET.PENDING);

        Trainer existing = new Trainer();
        existing.setKeycloakId("tid");
        existing.setStatus(TrainerStatusET.AKTIV);

        when(trainerRepository.findByKeycloakId("tid")).thenReturn(Optional.of(existing));
        when(trainerRepository.save(existing)).thenReturn(existing);

        profileService.updateTrainerStatus(trainer);

        assertEquals(TrainerStatusET.PENDING, existing.getStatus());
        verify(trainerRepository).save(existing);
    }

    @Test
    void testUpdateTrainerStatus_NewTrainer() {
        Trainer trainer = new Trainer();
        trainer.setKeycloakId("tid");
        trainer.setStatus(TrainerStatusET.AKTIV);

        when(trainerRepository.findByKeycloakId("tid")).thenReturn(Optional.empty());
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        profileService.updateTrainerStatus(trainer);

        verify(trainerRepository).save(trainer);
    }

    @Test
    void testSaveProfile_WithPassword() {
        UserAccount user = Mockito.mock(UserAccount.class);
        when(user.getNewPassword()).thenReturn("pass");
        doNothing().when(profileService).initWebClient();
        doNothing().when(profileService).updateUserAttributes(user);
        doNothing().when(profileService).updateUserPasswordToKeycloak(user);

        profileService.saveProfile(user);

        verify(profileService).updateUserAttributes(user);
        verify(profileService).updateUserPasswordToKeycloak(user);
    }

    @Test
    void testSaveProfile_WithoutPassword() {
        UserAccount user = Mockito.mock(UserAccount.class);
        when(user.getNewPassword()).thenReturn(null);
        doNothing().when(profileService).initWebClient();
        doNothing().when(profileService).updateUserAttributes(user);

        profileService.saveProfile(user);

        verify(profileService).updateUserAttributes(user);
        verify(profileService, never()).updateUserPasswordToKeycloak(user);
    }

    @Test
    void testDeleteProfile_Participant() {
        Participant participant = new Participant();
        participant.setKeycloakId("pid");
        doNothing().when(profileService).initWebClient();
        doNothing().when(profileService).deleteParticipantData("pid");
        doNothing().when(profileService).deleteUserFromKeycloak("pid");

        profileService.deleteProfile(participant);

        verify(profileService).deleteParticipantData("pid");
        verify(profileService).deleteUserFromKeycloak("pid");
    }

    @Test
    void testDeleteProfile_Trainer() {
        Trainer trainer = new Trainer();
        trainer.setKeycloakId("tid");
        doNothing().when(profileService).initWebClient();
        doNothing().when(profileService).deleteTrainerData("tid");
        doNothing().when(profileService).deleteUserFromKeycloak("tid");

        profileService.deleteProfile(trainer);

        verify(profileService).deleteTrainerData("tid");
        verify(profileService).deleteUserFromKeycloak("tid");
    }

    @Test
    void testDeleteParticipantData() {
        String keycloakId = "pid";
        List<CoursRegistration> regs = Arrays.asList(new CoursRegistration(), new CoursRegistration());
        Participant participant = new Participant();

        when(coursRegistrationRepository.findByParticipantKeycloakId(keycloakId)).thenReturn(regs);
        when(participantRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(participant));

        profileService.deleteParticipantData(keycloakId);

        verify(coursRegistrationRepository).deleteAll(regs);
        verify(participantRepository).delete(participant);
    }

    @Test
    void testDeleteTrainerData() {
        String keycloakId = "tid";
        Cours cours = Mockito.mock(Cours.class);
        List<Cours> courses = Arrays.asList(cours);
        List availabilities = Arrays.asList(new Object());
        List registrations = Arrays.asList(new Object());
        Trainer trainer = new Trainer();

        when(coursRepository.findAllWithAvailabilitiesByKeycloakId(keycloakId)).thenReturn(courses);
        when(cours.getAvailabilities()).thenReturn(availabilities);
        when(cours.getRegistrations()).thenReturn(registrations);
        when(trainerRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(trainer));

        profileService.deleteTrainerData(keycloakId);

        verify(availabilityRepository).deleteAll(availabilities);
        verify(coursRegistrationRepository).deleteAll(registrations);
        verify(coursRepository).delete(cours);
        verify(trainerRepository).delete(trainer);
    }
}