package com.skillhub.backend.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.skillhub.backend.models.RegistrationStatusET;
import com.skillhub.backend.models.entities.Availability;
import com.skillhub.backend.models.entities.Cours;
import com.skillhub.backend.models.entities.Location;
import com.skillhub.backend.models.entities.Participant;
import com.skillhub.backend.models.entities.Trainer;
import com.skillhub.backend.repositories.CoursRepository;
import com.skillhub.backend.repositories.LocationRepository;
import com.skillhub.backend.repositories.TrainerRepository;

@Service
public class CoursService {
    
    private static final String UPLOAD_DIR = "uploads/courses/";
    private final CoursRepository coursRepository;
    private final LocationRepository locationRepository;
    private final TrainerRepository trainerRepository;
    private final ProfileService profileService;

    CoursService(
        CoursRepository coursRepository, 
        LocationRepository locationRepository, 
        TrainerRepository trainerRepository,
        ProfileService profileService
    ) {
        this.coursRepository = coursRepository;
        this.locationRepository = locationRepository;
        this.trainerRepository = trainerRepository;
        this.profileService = profileService;
    }

    public List<Cours> getAllCourses(boolean userIsParticipant, String keycloakId) {
        try {
            List<Cours> courses;
            if(!userIsParticipant) 
                courses = coursRepository.findAllWithAvailabilitiesAndNotEnded();
            else 
                courses = coursRepository.findAllUnregisteredWithAvailabilitiesAndNotEnded(keycloakId, RegistrationStatusET.REGISTERED);

            courses = this.fetchTrainerAndParticipantData(courses);
            return courses;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching courses: " + e.getMessage(), e);
        }
    }

    public Cours getCourseById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            Cours cours = coursRepository.findById(uuid).orElseThrow(() -> new RuntimeException("Course not found: " + id));
            List<Cours> courses = List.of(cours);
            courses = this.fetchTrainerAndParticipantData(courses);
            return courses.get(0);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching course by ID: " + e.getMessage(), e);
        }
    }

    public List<Cours> getTrainerCourses(String keycloakId) {
        try {
            List<Cours> courses = coursRepository.findAllWithAvailabilitiesByKeycloakId(keycloakId);
            courses = this.fetchTrainerAndParticipantData(courses);
            return courses;
        } catch (Exception e) {
            System.err.println("Error fetching courses for trainer: " + e.getMessage());
            throw new RuntimeException("Error fetching courses for trainer: " + e.getMessage(), e);
        }
    }

    public List<Cours> getParticipantCourses(String keycloakId) {
        try {
            List<Cours> courses = coursRepository.findRegisteredCoursesByKeycloakId(keycloakId);
            courses = this.fetchTrainerAndParticipantData(courses);
            return courses;
        } catch (Exception e) {
            System.err.println("Error fetching courses for trainer: " + e.getMessage());
            throw new RuntimeException("Error fetching courses for trainer: " + e.getMessage(), e);
        }
    }

    public void deleteCourse(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            Cours cours = coursRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Course not found: " + id));
            coursRepository.delete(cours);
            // Remove pictures associated with the course
            if(cours.getPictureUrls() != null && !cours.getPictureUrls().isEmpty())
                removePictures(cours.getPictureUrls(), id);
        } catch (Exception e) {
            System.err.println("Error deleting course: " + e.getMessage());
            throw new RuntimeException("Error deleting course: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void saveCourse(Cours cours, List<MultipartFile> pictures, String keycloakId) {
        try {
            Trainer trainer = this.trainerRepository.findByKeycloakId(keycloakId).orElse(null);

            for (Availability availability : cours.getAvailabilities()) {
                availability.setCours(cours);

                availability.setTrainer(trainer);

                // Fetch managed Location entity
                if (availability.getLocation() != null && availability.getLocation().getId() != null) {
                    Location managedLocation = locationRepository.findById(availability.getLocation().getId())
                        .orElseThrow(() -> new RuntimeException("Location not found: " + availability.getLocation().getId()));
                    availability.setLocation(managedLocation);
                }
            }

            Cours existingCours = cours.getId() != null ? 
                coursRepository.findById(cours.getId()).orElse(null) : null;
            if (existingCours != null && existingCours.getPictureUrls() != null) 
                removePictures(existingCours.getPictureUrls(), existingCours.getId().toString());

            Cours coursSaved = this.coursRepository.save(cours);

            if (pictures != null) savePictures(coursSaved, pictures);
        } catch (Exception e) {
            throw new RuntimeException("Error saving course: " + e.getMessage(), e);
        }
    }

    void savePictures(Cours cours, List<MultipartFile> pictures) {
        try {
            List<String> pictureUrls = new ArrayList<>();
            String uploadDir = UPLOAD_DIR + cours.getId();
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            if (pictures != null) {
                for (MultipartFile picture : pictures) {
                    String fileName = System.currentTimeMillis() + "_" + picture.getOriginalFilename();
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(picture.getInputStream(), filePath);
                    // Assuming you serve files from /uploads/...
                    String url = uploadDir + "/" + fileName;
                    pictureUrls.add(url);
                }
            }
            cours.setPictureUrls(pictureUrls);
            this.coursRepository.save(cours);
        } catch (Exception e) {
            throw new RuntimeException("Error saving pictures: " + e.getMessage(), e);
        }
    }

    void removePictures(List<String> pictureUrls, String coursId) {
        try {
            for (String url : pictureUrls) {
                Path filePath = Paths.get(url);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            }

            Path uploadPath = Paths.get(UPLOAD_DIR + coursId);
            if (Files.exists(uploadPath)) {
                Files.delete(uploadPath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error removing pictures: " + e.getMessage(), e);
        }
    }

    List<Cours> fetchTrainerAndParticipantData(List<Cours> courses) {
        courses.forEach(cours -> {
            cours.getAvailabilities().forEach(availability -> {
                Trainer trainer = availability.getTrainer();
                if (trainer == null) throw new RuntimeException("Trainer not found for availability: " + availability.getId());
                trainer = (Trainer) this.profileService.getUserPublicInfo(trainer);
                trainer.setRole("trainer");
                availability.setTrainer(trainer);
            });

            cours.getRegistrations().forEach(registration -> {
                Participant participant = registration.getParticipant();
                if (participant == null) throw new RuntimeException("Participant not found for registration: " + registration.getId());
                participant = (Participant) this.profileService.getUserPublicInfo(participant);
                participant.setRole("participant");
                registration.setParticipant(participant);
            });
        });
        return courses;
    }
}