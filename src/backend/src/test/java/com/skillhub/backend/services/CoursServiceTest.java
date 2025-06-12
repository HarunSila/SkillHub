package com.skillhub.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import com.skillhub.backend.models.RegistrationStatusET;
import com.skillhub.backend.models.entities.*;
import com.skillhub.backend.repositories.CoursRepository;
import com.skillhub.backend.repositories.LocationRepository;
import com.skillhub.backend.repositories.TrainerRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CoursServiceTest {

    @Mock
    private CoursRepository coursRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private ProfileService profileService;

    @InjectMocks
    private CoursService coursService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        coursService = new CoursService(coursRepository, locationRepository, trainerRepository, profileService);
    }

    @Test
    void testGetAllCourses_NotParticipant() {
        List<Cours> courses = new ArrayList<>();
        Cours cours = mock(Cours.class);
        courses.add(cours);
        when(coursRepository.findAllWithAvailabilitiesAndNotEnded()).thenReturn(courses);
        when(cours.getAvailabilities()).thenReturn(Collections.emptyList());
        when(cours.getRegistrations()).thenReturn(Collections.emptyList());

        List<Cours> result = coursService.getAllCourses(false, "kid");
        assertEquals(1, result.size());
        verify(coursRepository).findAllWithAvailabilitiesAndNotEnded();
    }

    @Test
    void testGetAllCourses_Participant() {
        List<Cours> courses = new ArrayList<>();
        Cours cours = mock(Cours.class);
        courses.add(cours);
        when(coursRepository.findAllUnregisteredWithAvailabilitiesAndNotEnded(anyString(), eq(RegistrationStatusET.REGISTERED))).thenReturn(courses);
        when(cours.getAvailabilities()).thenReturn(Collections.emptyList());
        when(cours.getRegistrations()).thenReturn(Collections.emptyList());

        List<Cours> result = coursService.getAllCourses(true, "kid");
        assertEquals(1, result.size());
        verify(coursRepository).findAllUnregisteredWithAvailabilitiesAndNotEnded(anyString(), eq(RegistrationStatusET.REGISTERED));
    }

    @Test
    void testGetAllCourses_ThrowsException() {
        when(coursRepository.findAllWithAvailabilitiesAndNotEnded()).thenThrow(new RuntimeException("DB error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> coursService.getAllCourses(false, "kid"));
        assertTrue(ex.getMessage().contains("Error fetching courses"));
    }

    @Test
    void testGetCourseById_Success() {
        UUID uuid = UUID.randomUUID();
        Cours cours = mock(Cours.class);
        when(coursRepository.findById(uuid)).thenReturn(Optional.of(cours));
        when(cours.getAvailabilities()).thenReturn(Collections.emptyList());
        when(cours.getRegistrations()).thenReturn(Collections.emptyList());

        Cours result = coursService.getCourseById(uuid.toString());
        assertNotNull(result);
        verify(coursRepository).findById(uuid);
    }

    @Test
    void testGetCourseById_NotFound() {
        UUID uuid = UUID.randomUUID();
        when(coursRepository.findById(uuid)).thenReturn(Optional.empty());
        String uuidStr = uuid.toString();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> coursService.getCourseById(uuidStr));
        assertTrue(ex.getMessage().contains("Course not found"));
    }

    @Test
    void testGetCourseById_ThrowsException() {
        when(coursRepository.findById(any())).thenThrow(new RuntimeException("DB error"));
        String uuidStr = UUID.randomUUID().toString();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> coursService.getCourseById(uuidStr));
        assertTrue(ex.getMessage().contains("Error fetching course by ID"));
    }

    @Test
    void testGetTrainerCourses_Success() {
        List<Cours> courses = new ArrayList<>();
        Cours cours = mock(Cours.class);
        courses.add(cours);
        when(coursRepository.findAllWithAvailabilitiesByKeycloakId(anyString())).thenReturn(courses);
        when(cours.getAvailabilities()).thenReturn(Collections.emptyList());
        when(cours.getRegistrations()).thenReturn(Collections.emptyList());

        List<Cours> result = coursService.getTrainerCourses("kid");
        assertEquals(1, result.size());
        verify(coursRepository).findAllWithAvailabilitiesByKeycloakId("kid");
    }

    @Test
    void testGetTrainerCourses_ThrowsException() {
        when(coursRepository.findAllWithAvailabilitiesByKeycloakId(anyString())).thenThrow(new RuntimeException("DB error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> coursService.getTrainerCourses("kid"));
        assertTrue(ex.getMessage().contains("Error fetching courses for trainer"));
    }

    @Test
    void testGetParticipantCourses_Success() {
        List<Cours> courses = new ArrayList<>();
        Cours cours = mock(Cours.class);
        courses.add(cours);
        when(coursRepository.findRegisteredCoursesByKeycloakId(anyString())).thenReturn(courses);
        when(cours.getAvailabilities()).thenReturn(Collections.emptyList());
        when(cours.getRegistrations()).thenReturn(Collections.emptyList());

        List<Cours> result = coursService.getParticipantCourses("kid");
        assertEquals(1, result.size());
        verify(coursRepository).findRegisteredCoursesByKeycloakId("kid");
    }

    @Test
    void testGetParticipantCourses_ThrowsException() {
        when(coursRepository.findRegisteredCoursesByKeycloakId(anyString())).thenThrow(new RuntimeException("DB error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> coursService.getParticipantCourses("kid"));
        assertTrue(ex.getMessage().contains("Error fetching courses for trainer"));
    }

    @Test
    void testDeleteCourse_Success() {
        UUID uuid = UUID.randomUUID();
        Cours cours = mock(Cours.class);
        when(coursRepository.findById(uuid)).thenReturn(Optional.of(cours));
        when(cours.getPictureUrls()).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> coursService.deleteCourse(uuid.toString()));
        verify(coursRepository).delete(cours);
    }

    @Test
    void testDeleteCourse_WithPictures() {
        UUID uuid = UUID.randomUUID();
        Cours cours = mock(Cours.class);
        List<String> urls = Arrays.asList("uploads/courses/" + uuid + "/pic1.jpg");
        when(coursRepository.findById(uuid)).thenReturn(Optional.of(cours));
        when(cours.getPictureUrls()).thenReturn(urls);

        CoursService spyService = Mockito.spy(coursService);
        doNothing().when(spyService).removePictures(anyList(), anyString());

        spyService.deleteCourse(uuid.toString());
        verify(spyService).removePictures(urls, uuid.toString());
        verify(coursRepository).delete(cours);
    }

    @Test
    void testDeleteCourse_NotFound() {
        UUID uuid = UUID.randomUUID();
        when(coursRepository.findById(uuid)).thenReturn(Optional.empty());
        String uuidStr = uuid.toString();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> coursService.deleteCourse(uuidStr));
        assertTrue(ex.getMessage().contains("Course not found"));
    }

    @Test
    void testDeleteCourse_ThrowsException() {
        UUID uuid = UUID.randomUUID();
        when(coursRepository.findById(uuid)).thenThrow(new RuntimeException("DB error"));
        String uuidStr = uuid.toString();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> coursService.deleteCourse(uuidStr));
        assertTrue(ex.getMessage().contains("Error deleting course"));
    }

    @Test
    void testSaveCourse_Success() {
        Cours cours = mock(Cours.class);
        Availability availability = mock(Availability.class);
        List<Availability> availabilities = Collections.singletonList(availability);
        when(cours.getAvailabilities()).thenReturn(availabilities);
        when(availability.getLocation()).thenReturn(null);
        when(availability.getTrainer()).thenReturn(mock(Trainer.class));
        when(cours.getId()).thenReturn(null);
        when(coursRepository.save(cours)).thenReturn(cours);

        Trainer trainer = mock(Trainer.class);
        when(trainerRepository.findByKeycloakId(anyString())).thenReturn(Optional.of(trainer));

        assertDoesNotThrow(() -> coursService.saveCourse(cours, null, "kid"));
        verify(coursRepository).save(cours);
    }

    @Test
    void testSaveCourse_WithPictures() {
        Cours cours = mock(Cours.class);
        Availability availability = mock(Availability.class);
        List<Availability> availabilities = Collections.singletonList(availability);
        when(cours.getAvailabilities()).thenReturn(availabilities);
        when(availability.getLocation()).thenReturn(null);
        when(availability.getTrainer()).thenReturn(mock(Trainer.class));
        when(cours.getId()).thenReturn(null);
        when(coursRepository.save(cours)).thenReturn(cours);

        Trainer trainer = mock(Trainer.class);
        when(trainerRepository.findByKeycloakId(anyString())).thenReturn(Optional.of(trainer));

        MultipartFile file = mock(MultipartFile.class);
        CoursService spyService = Mockito.spy(coursService);
        doNothing().when(spyService).savePictures(any(Cours.class), anyList());

        spyService.saveCourse(cours, Collections.singletonList(file), "kid");
        verify(spyService).savePictures(eq(cours), anyList());
    }

    @Test
    void testSaveCourse_ThrowsException() {
        Cours cours = mock(Cours.class);
        when(cours.getAvailabilities()).thenThrow(new RuntimeException("DB error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> coursService.saveCourse(cours, null, "kid"));
        assertTrue(ex.getMessage().contains("Error saving course"));
    }
}