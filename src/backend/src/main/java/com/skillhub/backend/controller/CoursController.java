package com.skillhub.backend.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.skillhub.backend.models.entities.Availability;
import com.skillhub.backend.models.entities.Cours;
import com.skillhub.backend.services.CoursService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController @RequestMapping("/cours") @CrossOrigin(origins = "http://localhost:4200")
public class CoursController {
    
    private final CoursService coursService;

    CoursController(CoursService coursService) {
        this.coursService = coursService;
    }

    @GetMapping("/getAllCourses")
    public ResponseEntity<List<Cours>> getAllCourses() {
        try {
            List<Cours> courses = this.coursService.getAllCourses(false, null);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            System.err.println("Error fetching courses: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/getAllCourses")
    public ResponseEntity<List<Cours>> getAllCourses(@RequestBody String keycloakId) {
        try {
            List<Cours> courses = this.coursService.getAllCourses(true, keycloakId);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            System.err.println("Error fetching courses: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/getCoursById/{id}")
    public ResponseEntity<Cours> getCourseById(@PathVariable("id") String id) {
        try {
           Cours cours = this.coursService.getCourseById(id);
           return ResponseEntity.ok(cours);
        } catch (Exception e) {
            System.err.println("Error fetching course by ID: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/getTrainerCourses")
    public ResponseEntity<List<Cours>> getTrainerCourses(@RequestBody String keycloakId) {
        try {
            List<Cours> courses = this.coursService.getTrainerCourses(keycloakId);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            System.err.println("Error fetching trainer courses: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/getParticipantCourses")
    public ResponseEntity<List<Cours>> getParticipantCourses(@RequestBody String keycloakId) {
        try {
            List<Cours> courses = this.coursService.getParticipantCourses(keycloakId);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            System.err.println("Error fetching trainer courses: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    
    @DeleteMapping("/deleteCours/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable("id") String id) {
        try {
            this.coursService.deleteCourse(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error deleting course: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/saveCours")
    public ResponseEntity<Void> saveCours(
        @RequestParam("title") String title,
        @RequestParam("description") String description,
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate,
        @RequestParam("maxParticipants") Integer maxParticipants,
        @RequestParam("availability") String availability,
        @RequestParam(value = "pictures", required = false) List<MultipartFile> pictures,
        @RequestParam(value = "id", required = false) String id,
        @RequestParam("keycloakId") String keycloakId
    ) {
        try {
            LocalDate start = Instant.parse(startDate).atZone(ZoneId.of("UTC")).toLocalDate();
            LocalDate end = Instant.parse(endDate).atZone(ZoneId.of("UTC")).toLocalDate();

            Cours cours = new Cours();
            cours.setTitle(title);
            cours.setDescription(description);
            cours.setStartDate(start);
            cours.setEndDate(end);
            cours.setMaxParticipants(maxParticipants);
            if (id != null && !id.isEmpty()) {
                cours.setId(java.util.UUID.fromString(id));
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            List<Availability> availabilityList = mapper.readValue(
                availability,
                mapper.getTypeFactory().constructCollectionType(List.class, Availability.class)
            );
            cours.setAvailabilities(availabilityList);
            
            this.coursService.saveCourse(cours, pictures, keycloakId);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error saving course: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}