package com.skillhub.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillhub.backend.models.DayET;
import com.skillhub.backend.models.FilterRequestDTO;
import com.skillhub.backend.models.TimeRangeDTO;
import com.skillhub.backend.models.entities.Location;
import com.skillhub.backend.services.LocationManagementService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.UUID;


@RestController @RequestMapping("/location-management") @CrossOrigin(origins = "http://localhost:4200")
public class LocationManagementController {

    private final LocationManagementService locationManagementService;

    LocationManagementController(LocationManagementService locationManagementService) {
        this.locationManagementService = locationManagementService;
    }
    
    @GetMapping("/getLocations")
    public ResponseEntity<List<Location>> getLocations() {
        try {
            List<Location> locations = this.locationManagementService.getLocations();
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            System.err.println("Error fetching locations: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/saveLocation")
    public ResponseEntity<Location> saveLocation(@RequestBody Location location) {
        try{
            Location savedLocation = this.locationManagementService.saveLocation(location);
            return ResponseEntity.ok(savedLocation);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @DeleteMapping("/deleteLocation/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable String id) {
        try {
            UUID uuid = UUID.fromString(id);
            this.locationManagementService.deleteLocation(uuid);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/filterLocations")
    public ResponseEntity<Map<UUID, Map<DayET, List<TimeRangeDTO>>>> postMethodName(@RequestBody FilterRequestDTO entity) {
        try {
            Map<UUID, Map<DayET, List<TimeRangeDTO>>> filteredLocations = this.locationManagementService.filterLocations(entity);
            return ResponseEntity.ok(filteredLocations);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
