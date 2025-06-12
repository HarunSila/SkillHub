package com.skillhub.backend.controller;

import org.springframework.test.web.servlet.MockMvc;

import com.skillhub.backend.services.CoursService;
import com.skillhub.backend.models.entities.Cours;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

class CoursControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CoursService coursService;

    @InjectMocks
    private CoursController coursController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(coursController).build();
    }

    @Test
    void getAllCourses_shouldReturnListOfCourses() throws Exception {
        List<Cours> courses = Arrays.asList(new Cours(), new Cours());
        when(coursService.getAllCourses(false, null)).thenReturn(courses);

        mockMvc.perform(get("/cours/getAllCourses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void Post_getAllCourses_shouldReturnListOfCourses() throws Exception {
        Cours cours = new Cours();
        cours.setId(UUID.randomUUID());
        List<Cours> courses = Arrays.asList(cours);
        when(coursService.getAllCourses(true, "abc")).thenReturn(courses);

        mockMvc.perform(post("/cours/getAllCourses")
                .contentType("text/plain")
                .content("abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getCourseById_shouldReturnCourse() throws Exception {
        Cours cours = new Cours();
        cours.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        when(coursService.getCourseById("1")).thenReturn(cours);

        mockMvc.perform(get("/cours/getCoursById/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000001"));
    }

    @Test
    void getTrainerCourses_shouldReturnTrainerCourses() throws Exception {
        List<Cours> courses = Arrays.asList(new Cours());
        when(coursService.getTrainerCourses("trainerId")).thenReturn(courses);

        mockMvc.perform(post("/cours/getTrainerCourses")
                .contentType("text/plain")
                .content("trainerId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getParticipantCourses_shouldReturnParticipantCourses() throws Exception {
        List<Cours> courses = Arrays.asList(new Cours());
        when(coursService.getParticipantCourses("participantId")).thenReturn(courses);

        mockMvc.perform(post("/cours/getParticipantCourses")
                .contentType("text/plain")
                .content("participantId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void deleteCourse_shouldCallServiceAndReturnOk() throws Exception {
        doNothing().when(coursService).deleteCourse("1");

        mockMvc.perform(delete("/cours/deleteCours/1"))
                .andExpect(status().isOk());
    }
}
