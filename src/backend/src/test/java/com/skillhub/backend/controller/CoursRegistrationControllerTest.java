package com.skillhub.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.skillhub.backend.services.CoursRegistrationService;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillhub.backend.models.entities.Cours;
import com.skillhub.backend.models.entities.CoursRegistration;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class CoursRegistrationControllerTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private CoursRegistrationService coursRegistrationService;
    
    @InjectMocks
    private CoursRegistrationController coursRegistrationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(coursRegistrationController).build();
    }

    @Test
    void registerCours_ReturnsOkAndCours() throws Exception {
        CoursRegistration registration = new CoursRegistration();
        Cours cours = new Cours();
        when(coursRegistrationService.registerCours(any(CoursRegistration.class))).thenReturn(cours);

        mockMvc.perform(post("/cours-registration/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registration)))
                .andExpect(status().isOk());
        verify(coursRegistrationService).registerCours(any(CoursRegistration.class));
    }

    @Test
    void registerCours_WhenException_Returns500() throws Exception {
        CoursRegistration registration = new CoursRegistration();
        when(coursRegistrationService.registerCours(any(CoursRegistration.class))).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(post("/cours-registration/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registration)))
                .andExpect(status().isInternalServerError());
        verify(coursRegistrationService).registerCours(any(CoursRegistration.class));
    }

    @Test
    void assignRegistrationStatus_ReturnsOk() throws Exception {
        CoursRegistration registration = new CoursRegistration();

        mockMvc.perform(post("/cours-registration/assign-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registration)))
                .andExpect(status().isOk());
        verify(coursRegistrationService).assignRegistrationStatus(any(CoursRegistration.class));
    }

    @Test
    void assignRegistrationStatus_WhenException_Returns500() throws Exception {
        CoursRegistration registration = new CoursRegistration();
        doThrow(new RuntimeException("fail")).when(coursRegistrationService).assignRegistrationStatus(any(CoursRegistration.class));

        mockMvc.perform(post("/cours-registration/assign-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registration)))
                .andExpect(status().isInternalServerError());
        verify(coursRegistrationService).assignRegistrationStatus(any(CoursRegistration.class));
    }

    @Test
    void unregisterCours_ReturnsOk() throws Exception {
        String id = "123";
        mockMvc.perform(delete("/cours-registration/unregister/{id}", id))
                .andExpect(status().isOk());
        verify(coursRegistrationService).unregisterCours(id);
    }

    @Test
    void unregisterCours_WhenException_Returns500() throws Exception {
        String id = "123";
        doThrow(new RuntimeException("fail")).when(coursRegistrationService).unregisterCours(id);

        mockMvc.perform(delete("/cours-registration/unregister/{id}", id))
                .andExpect(status().isInternalServerError());
        verify(coursRegistrationService).unregisterCours(id);
    }
}