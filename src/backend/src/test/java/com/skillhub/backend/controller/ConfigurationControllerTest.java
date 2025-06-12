package com.skillhub.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.skillhub.backend.services.ConfigurationService;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillhub.backend.models.entities.Company;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ConfigurationControllerTest {
    
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ConfigurationService configurationService;

    @InjectMocks
    private ConfigurationController configurationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(configurationController).build();
    }

    @Test
    void getCompany_returnsCompany_whenServiceSucceeds() throws Exception {
        Company mockCompany = new Company();
        mockCompany.setName("TestCompany");
        when(configurationService.getCompany()).thenReturn(mockCompany);

        mockMvc.perform(get("/configuration/getCompany"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(mockCompany)));
    }

    @Test
    void getCompany_returns500_whenServiceThrows() throws Exception {
        when(configurationService.getCompany()).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(get("/configuration/getCompany"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void saveCompany_returnsOk_whenServiceSucceeds() throws Exception {
        Company company = new Company();
        company.setName("SaveTest");
        doNothing().when(configurationService).saveCompany(any(Company.class));

        mockMvc.perform(post("/configuration/saveCompany")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(company)))
            .andExpect(status().isOk());
    }

    @Test
    void saveCompany_returns500_whenServiceThrows() throws Exception {
        doThrow(new RuntimeException("fail")).when(configurationService).saveCompany(any(Company.class));
        Company company = new Company();
        company.setName("FailTest");

        mockMvc.perform(post("/configuration/saveCompany")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(company)))
            .andExpect(status().isInternalServerError());
    }
}