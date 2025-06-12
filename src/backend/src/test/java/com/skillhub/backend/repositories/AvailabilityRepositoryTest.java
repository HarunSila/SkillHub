package com.skillhub.backend.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.skillhub.backend.models.DayET;
import com.skillhub.backend.models.entities.Availability;
import com.skillhub.backend.models.entities.Cours;
import com.skillhub.backend.models.entities.Location;

@DataJpaTest
class AvailabilityRepositoryTest {
    
    @Autowired
    private AvailabilityRepository availabilityRepository;

    private static Location location;

    @BeforeAll
    static void setUp(
        @Autowired LocationRepository locationRepository, 
        @Autowired CoursRepository coursRepository, 
        @Autowired AvailabilityRepository availabilityRepository
    ) {
        Availability availability = new Availability();
        availability.setWeekday(DayET.MONDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(11, 0));

        location = new Location();
        location.setName("Test Location");
        location.setCapacity(100);
        location = locationRepository.save(location);
        availability.setLocation(location);

        Cours cours = new Cours();
        cours.setStartDate(LocalDate.of(2023, 10, 1));
        cours.setEndDate(LocalDate.of(2023, 10, 31));
        cours.setAvailabilities(List.of(availability));
        availability.setCours(cours);
        coursRepository.save(cours);
    }

    @Test
    void testFindByLocationAndDayAndStartdateAndEnddate_ExactStartAndEndDate() {
        List<Availability> results = availabilityRepository
            .findByLocationAndDayAndStartdateAndEnddate(
                location,
                DayET.MONDAY,
                LocalDate.of(2023, 10, 1),
                LocalDate.of(2023, 10, 31)
            );
        assertEquals(1, results.size(), "Should find availability for exact date range");
    }

    @Test
    void testFindByLocationAndDayAndStartdateAndEnddate_StartDateEqualsEndDate() {
        List<Availability> results = availabilityRepository
            .findByLocationAndDayAndStartdateAndEnddate(
                location,
                DayET.MONDAY,
                LocalDate.of(2023, 10, 15),
                LocalDate.of(2023, 10, 15)
            );
        assertEquals(1, results.size(), "Should find availability when startDate equals endDate and is within range");
    }

    @Test
    void testFindByLocationAndDayAndStartdateAndEnddate_BeforeRange() {
        List<Availability> results = availabilityRepository
            .findByLocationAndDayAndStartdateAndEnddate(
                location,
                DayET.MONDAY,
                LocalDate.of(2023, 9, 1),
                LocalDate.of(2023, 9, 30)
            );
        assertEquals(0, results.size(), "Should return no results for date range before availability");
    }
}
