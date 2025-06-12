package com.skillhub.backend.services;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillhub.backend.models.DayET;
import com.skillhub.backend.models.FilterRequestDTO;
import com.skillhub.backend.models.TimeRangeDTO;
import com.skillhub.backend.models.entities.Availability;
import com.skillhub.backend.models.entities.Cours;
import com.skillhub.backend.models.entities.Location;
import com.skillhub.backend.repositories.AvailabilityRepository;
import com.skillhub.backend.repositories.CoursRepository;
import com.skillhub.backend.repositories.LocationRepository;

@Service
public class LocationManagementService {

    private final LocationRepository locationRepository;
    private final TimeSlotService timeSlotService;
    private final AvailabilityRepository availabilityRepository;
    private final CoursService coursService;
    

    LocationManagementService(
        LocationRepository locationRepository, 
        TimeSlotService timeSlotService,
        AvailabilityRepository availabilityRepository,
        CoursService coursService
    ) {
        this.locationRepository = locationRepository;
        this.timeSlotService = timeSlotService;
        this.availabilityRepository = availabilityRepository;
        this.coursService = coursService;
    }

    public List<Location> getLocations() {
        try {
            return locationRepository.findAll();
        } catch (Exception e) {
            System.err.println("Error fetching locations: " + e.getMessage());
            throw new RuntimeException("Error fetching locations: " + e.getMessage());
        }
    }

    @Transactional
    public Location saveLocation(Location location) {
        try {
            Location savedLocation = locationRepository.save(location);
            return savedLocation;
        } catch (Exception e) {
            System.err.println("Error saving location: " + e.getMessage());
            throw new RuntimeException("Error saving location: " + e.getMessage());
        }
    }

    public void deleteLocation(UUID id) {
        try {
            List<Availability> availabilities = this.availabilityRepository.findByLocation(id);
            if(availabilities.isEmpty()) {
                locationRepository.deleteById(id);
            } else {
                this.coursService.deleteCourse(availabilities.get(0).getCours().getId().toString());
                deleteLocation(id);
            }
        } catch (Exception e) {
            System.err.println("Error deleting location: " + e.getMessage());
            throw new RuntimeException("Error deleting location: " + e.getMessage());
        }
    }

    public Map<UUID, Map<DayET, List<TimeRangeDTO>>> filterLocations(FilterRequestDTO filterRequest) {
        Map<UUID, Map<DayET, List<TimeRangeDTO>>> filteredLocations = new HashMap<>();

        try {
            // Level 1 Filter: Nur aktive Locations mit ausreichender max. Teilnehmerzahl
            List<Location> locations = locationRepository.findByActiveAndMaxPeopleGreaterThanEqual(true, filterRequest.getMaxParticipants());
            if (locations.isEmpty()) {
                System.out.println("No active locations found with the specified maximum participants.");
                return filteredLocations;
            }

            // Level 2 Filter: Verfügbare Zeitfenster entsprechend der Öffnungszeiten
            for (DayET day : DayET.values()) {
                List<LocalTime> availableSlots = timeSlotService.getAvailableSlots(day);
                if (availableSlots.isEmpty()) continue;
    
                // Level 3 Filter: Überprüfe ob es verfügbare Zeitfenster für die Locations gibt
                for (Location location : locations) {
                    Map<DayET, List<TimeRangeDTO>> daySlots = filteredLocations.computeIfAbsent(location.getId(), k -> new HashMap<>());
                    List<LocalTime> timeSlots = timeSlotService.getAvailableTimeSlotsForLocation(
                        location, day, availableSlots, filterRequest.getStartDate(), filterRequest.getEndDate()
                    );
                    List<TimeRangeDTO> timeRangeList = new ArrayList<>();
                    
                    // Level 4 Filter: Erzeuge Zeitfenster basierend auf den verfügbaren Zeitfenstern 
                    // und der benötigten Dauer für den Kurs
                    timeSlots.forEach(ts -> {
                        LocalTime start = ts;
                        LocalTime end = ts.plusMinutes(filterRequest.getDuration()- 30);

                        while (start.isBefore(end)) {
                            start = start.plusMinutes(30);
                            if (!timeSlots.contains(start)) break;
                        }

                        if (timeSlots.contains(start) && start.equals(end))
                            timeRangeList.add(new TimeRangeDTO(ts, end.plusMinutes(30)));
                    });

                    if (!timeRangeList.isEmpty()) {
                        daySlots.put(day, timeRangeList);
                    }
                }
            }

            return filteredLocations;
        } catch (Exception e) {
            System.err.println("Error filtering locations: " + e.getMessage());
            throw new RuntimeException("Error filtering locations: " + e.getMessage());
        }
    }
}
