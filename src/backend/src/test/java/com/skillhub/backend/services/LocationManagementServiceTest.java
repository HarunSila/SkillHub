package com.skillhub.backend.services;

import com.skillhub.backend.models.DayET;
import com.skillhub.backend.models.FilterRequestDTO;
import com.skillhub.backend.models.TimeRangeDTO;
import com.skillhub.backend.models.entities.Location;
import com.skillhub.backend.repositories.AvailabilityRepository;
import com.skillhub.backend.repositories.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LocationManagementServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private TimeSlotService timeSlotService;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private CoursService coursService;

    @InjectMocks @Spy
    private LocationManagementService locationManagementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        locationManagementService = Mockito.spy(
            new LocationManagementService(locationRepository, timeSlotService, availabilityRepository, coursService)
            );
    }

    @Test
    void testGetLocationsReturnsList() {
        List<Location> locations = Arrays.asList(new Location(), new Location());
        when(locationRepository.findAll()).thenReturn(locations);

        List<Location> result = locationManagementService.getLocations();

        assertEquals(2, result.size());
        verify(locationRepository, times(1)).findAll();
    }

    @Test
    void testGetLocationsThrowsException() {
        when(locationRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> locationManagementService.getLocations());
        assertTrue(ex.getMessage().contains("Error fetching locations"));
    }

    @Test
    void testSaveLocationSuccess() {
        Location location = new Location();
        when(locationRepository.save(location)).thenReturn(location);

        Location saved = locationManagementService.saveLocation(location);

        assertEquals(location, saved);
        verify(locationRepository, times(1)).save(location);
    }

    @Test
    void testSaveLocationThrowsException() {
        Location location = new Location();
        when(locationRepository.save(location)).thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> locationManagementService.saveLocation(location));
        assertTrue(ex.getMessage().contains("Error saving location"));
    }

    @Test
    void testDeleteLocationSuccess() {
        UUID id = UUID.randomUUID();
        doNothing().when(locationRepository).deleteById(id);

        assertDoesNotThrow(() -> locationManagementService.deleteLocation(id));
        verify(locationRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteLocationThrowsException() {
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("DB error")).when(locationRepository).deleteById(id);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> locationManagementService.deleteLocation(id));
        assertTrue(ex.getMessage().contains("Error deleting location"));
    }

    @Test
    void testFilterLocationsReturnsEmptyIfNoLocations() {
        FilterRequestDTO filterRequest = mock(FilterRequestDTO.class);
        when(filterRequest.getMaxParticipants()).thenReturn(10);
        when(locationRepository.findByActiveAndMaxPeopleGreaterThanEqual(true, 10)).thenReturn(Collections.emptyList());

        Map<UUID, Map<DayET, List<TimeRangeDTO>>> result = locationManagementService.filterLocations(filterRequest);

        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterLocationsWithAvailableSlots() {
        FilterRequestDTO filterRequest = mock(FilterRequestDTO.class);
        when(filterRequest.getMaxParticipants()).thenReturn(5);
        when(filterRequest.getDuration()).thenReturn(60);
        when(filterRequest.getStartDate()).thenReturn(LocalDate.now());
        when(filterRequest.getEndDate()).thenReturn(LocalDate.now().plusDays(1));

        Location location = mock(Location.class);
        UUID locationId = UUID.randomUUID();
        when(location.getId()).thenReturn(locationId);

        List<Location> locations = Collections.singletonList(location);
        when(locationRepository.findByActiveAndMaxPeopleGreaterThanEqual(true, 5)).thenReturn(locations);

        for (DayET day : DayET.values()) {
            List<LocalTime> availableSlots = Arrays.asList(LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0));
            when(timeSlotService.getAvailableSlots(day)).thenReturn(availableSlots);
            when(timeSlotService.getAvailableTimeSlotsForLocation(
                    eq(location), eq(day), eq(availableSlots), any(), any()))
                .thenReturn(availableSlots);
        }

        Map<UUID, Map<DayET, List<TimeRangeDTO>>> result = locationManagementService.filterLocations(filterRequest);

        assertFalse(result.isEmpty());
        assertTrue(result.containsKey(locationId));
        
        boolean hasTimeRanges = result.values().stream()
                .flatMap(m -> m.values().stream())
                .anyMatch(list -> !list.isEmpty());
        assertTrue(hasTimeRanges);
    }

    @Test
    void testFilterLocationsHandlesException() {
        FilterRequestDTO filterRequest = mock(FilterRequestDTO.class);
        when(filterRequest.getMaxParticipants()).thenReturn(1);
        when(locationRepository.findByActiveAndMaxPeopleGreaterThanEqual(true, 1))
                .thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> locationManagementService.filterLocations(filterRequest));
        assertTrue(ex.getMessage().contains("Error filtering locations"));
    }
}
