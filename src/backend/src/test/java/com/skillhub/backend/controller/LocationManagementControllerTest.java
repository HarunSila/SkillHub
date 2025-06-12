package com.skillhub.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.skillhub.backend.services.LocationManagementService;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillhub.backend.models.DayET;
import com.skillhub.backend.models.FilterRequestDTO;
import com.skillhub.backend.models.TimeRangeDTO;
import com.skillhub.backend.models.entities.Location;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.util.*;

class LocationManagementControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private LocationManagementService locationManagementService;

    @InjectMocks
    private LocationManagementController locationManagementController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(locationManagementController).build();
    }

    @Test
    void getLocations_ReturnsOkAndList() throws Exception {
        List<Location> locations = Arrays.asList(new Location(), new Location());
        when(locationManagementService.getLocations()).thenReturn(locations);

        mockMvc.perform(get("/location-management/getLocations"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(locations)));
        verify(locationManagementService).getLocations();
    }

    @Test
    void getLocations_WhenException_Returns500() throws Exception {
        when(locationManagementService.getLocations()).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(get("/location-management/getLocations"))
                .andExpect(status().isInternalServerError());
        verify(locationManagementService).getLocations();
    }

    @Test
    void saveLocation_ReturnsOkAndLocation() throws Exception {
        Location location = new Location();
        when(locationManagementService.saveLocation(any(Location.class))).thenReturn(location);

        mockMvc.perform(post("/location-management/saveLocation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(location)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(location)));
        verify(locationManagementService).saveLocation(any(Location.class));
    }

    @Test
    void saveLocation_WhenException_Returns500() throws Exception {
        Location location = new Location();
        when(locationManagementService.saveLocation(any(Location.class))).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(post("/location-management/saveLocation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(location)))
                .andExpect(status().isInternalServerError());
        verify(locationManagementService).saveLocation(any(Location.class));
    }

    @Test
    void deleteLocation_ReturnsOk() throws Exception {
        UUID uuid = UUID.randomUUID();

        mockMvc.perform(delete("/location-management/deleteLocation/{id}", uuid.toString()))
                .andExpect(status().isOk());
        verify(locationManagementService).deleteLocation(uuid);
    }

    @Test
    void deleteLocation_WhenException_Returns500() throws Exception {
        UUID uuid = UUID.randomUUID();
        doThrow(new RuntimeException("fail")).when(locationManagementService).deleteLocation(uuid);

        mockMvc.perform(delete("/location-management/deleteLocation/{id}", uuid.toString()))
                .andExpect(status().isInternalServerError());
        verify(locationManagementService).deleteLocation(uuid);
    }

    @Test
    void filterLocations_ReturnsOkAndMap() throws Exception {
        FilterRequestDTO filterRequestDTO = new FilterRequestDTO();
        Map<UUID, Map<DayET, List<TimeRangeDTO>>> filtered = new HashMap<>();
        when(locationManagementService.filterLocations(any(FilterRequestDTO.class))).thenReturn(filtered);

        mockMvc.perform(post("/location-management/filterLocations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(filtered)));
        verify(locationManagementService).filterLocations(any(FilterRequestDTO.class));
    }

    @Test
    void filterLocations_WhenException_Returns500() throws Exception {
        FilterRequestDTO filterRequestDTO = new FilterRequestDTO();
        when(locationManagementService.filterLocations(any(FilterRequestDTO.class))).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(post("/location-management/filterLocations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterRequestDTO)))
                .andExpect(status().isInternalServerError());
        verify(locationManagementService).filterLocations(any(FilterRequestDTO.class));
    }
}