package com.skillhub.backend.services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.skillhub.backend.models.DayET;
import com.skillhub.backend.models.OpeningTimeT;
import com.skillhub.backend.models.entities.Availability;
import com.skillhub.backend.models.entities.Company;
import com.skillhub.backend.models.entities.Location;
import com.skillhub.backend.repositories.AvailabilityRepository;
import com.skillhub.backend.repositories.CompanyRepository;

// Die Service-Klasse bietet Methoden zur Verwaltung und Abfrage verfügbarer Zeitfenster
// für die Öffnungszeiten eines Unternehmens und gebuchte Slots für bestimmte Standorte.
@Service
public class TimeSlotService {
    private static final LocalTime[] AVAILABLE_SLOTS = {
        LocalTime.of(0, 0), LocalTime.of(0, 30),
        LocalTime.of(1, 0), LocalTime.of(1, 30),
        LocalTime.of(2, 0), LocalTime.of(2, 30),
        LocalTime.of(3, 0), LocalTime.of(3, 30),
        LocalTime.of(4, 0), LocalTime.of(4, 30),
        LocalTime.of(5, 0), LocalTime.of(5, 30),
        LocalTime.of(6, 0), LocalTime.of(6, 30),
        LocalTime.of(7, 0), LocalTime.of(7, 30),
        LocalTime.of(8, 0), LocalTime.of(8, 30),
        LocalTime.of(9, 0), LocalTime.of(9, 30),
        LocalTime.of(10, 0), LocalTime.of(10, 30),
        LocalTime.of(11, 0), LocalTime.of(11, 30),
        LocalTime.of(12, 0), LocalTime.of(12, 30),
        LocalTime.of(13, 0), LocalTime.of(13, 30),
        LocalTime.of(14, 0), LocalTime.of(14, 30),
        LocalTime.of(15, 0), LocalTime.of(15, 30),
        LocalTime.of(16, 0), LocalTime.of(16, 30),
        LocalTime.of(17, 0), LocalTime.of(17, 30),
        LocalTime.of(18, 0), LocalTime.of(18, 30),
        LocalTime.of(19, 0), LocalTime.of(19, 30),
        LocalTime.of(20, 0), LocalTime.of(20, 30),
        LocalTime.of(21, 0), LocalTime.of(21, 30),
        LocalTime.of(22, 0), LocalTime.of(22, 30),
        LocalTime.of(23, 0), LocalTime.of(23, 30) 
    };

    private final CompanyRepository companyRepository;
    private final AvailabilityRepository availabilityRepository;

    TimeSlotService(
        CompanyRepository companyRepository, 
        AvailabilityRepository availabilityRepository
    ) {
        this.companyRepository = companyRepository;
        this.availabilityRepository = availabilityRepository;
    }

     /**
      * Diese Methode ruft die verfügbaren Zeitfenster für einen bestimmten Wochentag
      * basierend auf den Öffnungszeiten des Unternehmens ab. Sie gibt eine Liste von
      * LocalTime-Objekten zurück, die die verfügbaren Slots für diesen Tag repräsentieren.
      * @param dayOfWeek der Wochentag, für den die verfügbaren Slots abgerufen werden sollen
      * @return eine Liste von LocalTime-Objekten, die die verfügbaren Slots für diesen Tag repräsentieren
      */
    public List<LocalTime> getAvailableSlots(DayET dayOfWeek) {
        List<Company> companies = companyRepository.findAll();
        if (companies.isEmpty()) return List.of();

        Company company = companies.get(0);
        List<OpeningTimeT> openingTimes = company.getOpeningTimes(); 
        if (openingTimes == null || openingTimes.isEmpty()) return List.of();

        for (OpeningTimeT openingTime : openingTimes) {
            if (openingTime.getWeekday() == dayOfWeek) {
                LocalTime start = openingTime.getStartTime();
                LocalTime end = openingTime.getEndTime();
                List<LocalTime> availableSlots = new ArrayList<>();

                for (LocalTime slot : AVAILABLE_SLOTS) {
                    if (!slot.isBefore(start) && !slot.isAfter(end) && !slot.equals(end)) {
                        availableSlots.add(slot);
                    }
                }
                return availableSlots;
            }
        }
        return List.of();
    }

     /**
      * Diese Methode gibt die verfügbaren Zeitfenster für einen bestimmten Standort
      * an einem bestimmten Wochentag innerhalb eines Datumsbereichs zurück.
        * Sie filtert die verfügbaren Slots basierend auf den Buchungen und der Verfügbarkeit
        * der Kurse.
      * @param location der Standort, für den die verfügbaren Zeitfenster abgerufen werden sollen
      * @param day der Wochentag, für den die verfügbaren Zeitfenster abgerufen werden sollen
      * @param availableSlots die anfängliche Liste der verfügbaren Zeitfenster
      * @param startDate der Starttermin des Datumsbereichs, für den die Verfügbarkeit geprüft werden soll
      * @param endDate der Endtermin des Datumsbereichs, für den die Verfügbarkeit geprüft werden soll
      * @return eine Liste der verfügbaren Zeitfenster nach dem Herausfiltern der gebuchten Slots
      */
    public List<LocalTime> getAvailableTimeSlotsForLocation(
        Location location, DayET day, List<LocalTime> availableSlots,
        LocalDate startDate, LocalDate endDate
    ) {
        List<LocalTime> resultSlots = new ArrayList<>(availableSlots);
        List<Availability> availabilities = availabilityRepository.findByLocationAndDayAndStartdateAndEnddate(location, day, startDate, endDate);
                
        for (Availability availability : availabilities) {
            // Entferne gebuchte Slots, die nicht verfügbar sind
            resultSlots.removeIf(slot -> 
                !slot.isBefore(availability.getStartTime()) && !slot.isAfter(availability.getEndTime().minusMinutes(30))
            );
        }

        return resultSlots;
    }
}