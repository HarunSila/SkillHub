package com.skillhub.backend.services;

import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.skillhub.backend.models.DayET;
import com.skillhub.backend.models.OpeningTimeT;
import com.skillhub.backend.models.entities.Company;
import com.skillhub.backend.repositories.CompanyRepository;

class TimeSlotServiceTest {
    
    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private TimeSlotService timeSlotService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAvailableSlots() {
        DayET dayOfWeek = DayET.MONDAY;
        Company company = new Company();
        company.setOpeningTimes(List.of(
            new OpeningTimeT(DayET.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0))
        ));

        when(companyRepository.findAll()).thenReturn(List.of(company));

        List<LocalTime> availableSlots = timeSlotService.getAvailableSlots(dayOfWeek);
        System.out.println("Available slots: " + availableSlots);
        assert availableSlots.size() == 16;
        assert !availableSlots.contains(LocalTime.of(8, 30));
        assert availableSlots.contains(LocalTime.of(9, 0));
        assert availableSlots.contains(LocalTime.of(9, 30));
        assert availableSlots.contains(LocalTime.of(16, 30));
        assert !availableSlots.contains(LocalTime.of(17, 0));
    }
    
    @Test
    void testAvailableSlotsWithNoCompany() {
        DayET dayOfWeek = DayET.MONDAY;

        when(companyRepository.findAll()).thenReturn(List.of());

        List<LocalTime> availableSlots = timeSlotService.getAvailableSlots(dayOfWeek);
        assert availableSlots.isEmpty(); 
    }

    @Test
    void testAvailableSlotsWithNoOpeningTimes() {
        DayET dayOfWeek = DayET.MONDAY;
        Company company = new Company();
        company.setOpeningTimes(List.of());

        when(companyRepository.findAll()).thenReturn(List.of(company));

        List<LocalTime> availableSlots = timeSlotService.getAvailableSlots(dayOfWeek);
        assert availableSlots.isEmpty(); 
    }

    @Test
    void testAvailableSlotsWithNoMatchingDay() {
        DayET dayOfWeek = DayET.TUESDAY;
        Company company = new Company();
        company.setOpeningTimes(List.of(
            new OpeningTimeT(DayET.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0))
        ));

        when(companyRepository.findAll()).thenReturn(List.of(company));

        List<LocalTime> availableSlots = timeSlotService.getAvailableSlots(dayOfWeek);
        assert availableSlots.isEmpty(); 
    }
}
