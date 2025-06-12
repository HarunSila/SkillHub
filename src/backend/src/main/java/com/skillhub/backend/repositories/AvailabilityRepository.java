package com.skillhub.backend.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.skillhub.backend.models.DayET;
import com.skillhub.backend.models.entities.Availability;
import com.skillhub.backend.models.entities.Cours;
import com.skillhub.backend.models.entities.Location;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {
    
    @Query("SELECT a FROM Availability a WHERE a.location = :location AND a.weekday = :weekday AND a.cours.startDate <= :endDate AND a.cours.endDate >= :startDate")
    List<Availability> findByLocationAndDayAndStartdateAndEnddate(
        @Param(value = "location") Location location, 
        @Param(value = "weekday") DayET weekday, 
        @Param(value = "startDate") LocalDate startDate, 
        @Param(value = "endDate") LocalDate endDate
    );

    @Query("SELECT a FROM Availability a WHERE a.location.id = :locationId")
    List<Availability> findByLocation(UUID locationId);

    void deleteByCours(Cours cours);
}
