package com.skillhub.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.skillhub.backend.services.ProfileService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillhub.backend.models.TrainerStatusET;
import com.skillhub.backend.models.entities.Participant;
import com.skillhub.backend.models.entities.Trainer;
import com.skillhub.backend.models.entities.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.util.Arrays;
import java.util.List;

class ProfileControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private MockMvc mockMvc;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private ProfileController profileController;
   
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(profileController).build();
    }

    @Test
    void getProfiles_ReturnsOkAndList_NoDuplicateJsonKeys() throws Exception {
        Participant participant = new Participant();
        participant.setKeycloakId("p1");
        participant.setName("Alice");
        participant.setSurname("Smith");
        participant.setEmail("alice@example.com");

        Trainer trainer = new Trainer();
        trainer.setKeycloakId("t1");
        trainer.setName("Bob");
        trainer.setSurname("Jones");
        trainer.setEmail("bob@example.com");

        List<UserAccount> profiles = Arrays.asList(participant, trainer);
        when(profileService.getProfiles()).thenReturn(profiles);

        mockMvc.perform(get("/profile/getProfiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].keycloakId").value("p1"))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].keycloakId").value("t1"))
                .andExpect(jsonPath("$[1].name").value("Bob"));
        verify(profileService).getProfiles();
    }

    @Test
    void getProfiles_WhenException_Returns500() throws Exception {
        when(profileService.getProfiles()).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(get("/profile/getProfiles"))
                .andExpect(status().isInternalServerError());
        verify(profileService).getProfiles();
    }

    @Test
    void getTrainerStatus_ReturnsOk() throws Exception {
        String keycloakId = "abc123";
        TrainerStatusET status = TrainerStatusET.AKTIV;
        when(profileService.getTrainerStatus(anyString())).thenReturn(status);

        mockMvc.perform(post("/profile/getTrainerStatus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(keycloakId)))
                .andExpect(status().isOk())
                .andExpect(content().string("\"" + status.name() + "\""));
        verify(profileService).getTrainerStatus(anyString());
    }

    @Test
    void getTrainerStatus_WhenException_Returns500() throws Exception {
        String keycloakId = "abc123";
        when(profileService.getTrainerStatus(anyString())).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(post("/profile/getTrainerStatus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(keycloakId)))
                .andExpect(status().isInternalServerError());
        verify(profileService).getTrainerStatus(anyString());
    }

    @Test
    void updateTrainerStatus_ReturnsOk() throws Exception {
        Trainer trainer = new Trainer();
        trainer.setKeycloakId("abc123");

        mockMvc.perform(post("/profile/updateTrainerStatus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainer)))
                .andExpect(status().isOk());
        verify(profileService).updateTrainerStatus(any(Trainer.class));
    }

    @Test
    void updateTrainerStatus_InvalidTrainer_ReturnsBadRequest() throws Exception {
        Trainer trainer = new Trainer();
        trainer.setKeycloakId(""); // blank

        mockMvc.perform(post("/profile/updateTrainerStatus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainer)))
                .andExpect(status().isBadRequest());
        verify(profileService, never()).updateTrainerStatus(any(Trainer.class));
    }

    @Test
    void updateTrainerStatus_WhenException_Returns500() throws Exception {
        Trainer trainer = new Trainer();
        trainer.setKeycloakId("abc123");
        doThrow(new RuntimeException("fail")).when(profileService).updateTrainerStatus(any(Trainer.class));

        mockMvc.perform(post("/profile/updateTrainerStatus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainer)))
                .andExpect(status().isInternalServerError());
        verify(profileService).updateTrainerStatus(any(Trainer.class));
    }

    @Test
    void saveProfile_Valid_ReturnsOk() throws Exception {
        UserAccount user = new Participant();
        user.setKeycloakId("abc123");
        user.setName("John");
        user.setSurname("Doe");
        user.setEmail("john.doe@email.com");

        mockMvc.perform(post("/profile/saveProfile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
        verify(profileService).saveProfile(any(UserAccount.class));
    }

    @Test
    void saveProfile_Invalid_ReturnsBadRequest() throws Exception {
        UserAccount user = new Trainer(); // invalid, missing fields

        mockMvc.perform(post("/profile/saveProfile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
        verify(profileService, never()).saveProfile(any(UserAccount.class));
    }

    @Test
    void saveProfile_WhenException_Returns500() throws Exception {
        UserAccount user = new Trainer();
        user.setKeycloakId("abc123");
        user.setName("John");
        user.setSurname("Doe");
        user.setEmail("john.doe@email.com");
        doThrow(new RuntimeException("fail")).when(profileService).saveProfile(any(UserAccount.class));

        mockMvc.perform(post("/profile/saveProfile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isInternalServerError());
        verify(profileService).saveProfile(any(UserAccount.class));
    }

    @Test
    void deleteProfile_Valid_ReturnsOk() throws Exception {
        UserAccount user = new Participant();
        user.setKeycloakId("abc123");
        user.setName("John");
        user.setSurname("Doe");
        user.setEmail("john.doe@email.com");

        mockMvc.perform(post("/profile/deleteProfile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
        verify(profileService).deleteProfile(any(UserAccount.class));
    }

    @Test
    void deleteProfile_Invalid_ReturnsBadRequest() throws Exception {
        UserAccount user = new Participant(); // invalid, missing fields

        mockMvc.perform(post("/profile/deleteProfile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
        verify(profileService, never()).deleteProfile(any(UserAccount.class));
    }

    @Test
    void deleteProfile_WhenException_Returns500() throws Exception {
        UserAccount user = new Trainer();
        user.setKeycloakId("abc123");
        user.setName("John");
        user.setSurname("Doe");
        user.setEmail("john.doe@email.com");
        doThrow(new RuntimeException("fail")).when(profileService).deleteProfile(any(UserAccount.class));

        mockMvc.perform(post("/profile/deleteProfile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isInternalServerError());
        verify(profileService).deleteProfile(any(UserAccount.class));
    }
}