package com.skillhub.backend.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.skillhub.backend.models.LocationStatusT;
import com.skillhub.backend.models.entities.Location;

@DataJpaTest
class LocationRepositoryTest {
    
    @Autowired
    private LocationRepository locationRepository;

    @Test
    void testFindByActiveAndMaxPeopleGreaterThanEqual() {
        Location testLocation = new Location();
        LocationStatusT status = new LocationStatusT();
        status.setActive(true);
        testLocation.setStatus(status);
        testLocation.setCapacity(50);
        locationRepository.save(testLocation);

        List<Location> locations = locationRepository.findByActiveAndMaxPeopleGreaterThanEqual(true, 50);

        assertFalse(locations.isEmpty());
        assertEquals(1, locations.size());
        assertEquals(testLocation.getId(), locations.get(0).getId());
    }

    @Test
    void testFindByActiveAndMaxPeopleGreaterThanEqual_NoMatch() {
        Location testLocation = new Location();
        LocationStatusT status = new LocationStatusT();
        status.setActive(false);
        testLocation.setStatus(status);
        testLocation.setCapacity(30);
        locationRepository.save(testLocation);

        List<Location> locations = locationRepository.findByActiveAndMaxPeopleGreaterThanEqual(true, 50);

        assertEquals(0, locations.size());
    }
}