package com.skillhub.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.skillhub.backend.models.entities.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

   @Query("SELECT l FROM Location l WHERE l.status.active = :active AND l.capacity >= :capacity")
    List<Location> findByActiveAndMaxPeopleGreaterThanEqual(@Param("active") boolean active, 
                                                            @Param("capacity") int capacity);
}
