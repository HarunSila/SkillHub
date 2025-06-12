package com.skillhub.backend.configuration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import com.skillhub.backend.models.AddressT;
import com.skillhub.backend.models.DayET;
import com.skillhub.backend.models.LocationStatusT;
import com.skillhub.backend.models.OpeningTimeT;
import com.skillhub.backend.models.entities.Company;
import com.skillhub.backend.models.entities.Equipment;
import com.skillhub.backend.models.entities.Location;
import com.skillhub.backend.repositories.CompanyRepository;
import com.skillhub.backend.repositories.EquipmentRepository;
import com.skillhub.backend.repositories.LocationRepository;

@Configuration
public class TestDataInitializer {
    
    @Bean @Transactional
    CommandLineRunner initLocationData(
        LocationRepository locationRepository, 
        EquipmentRepository equipmentRepository,
        CompanyRepository companyRepository

    ) {
        return args -> {
            List<Location> locations = locationRepository.saveAll(createTestLocations());
            equipmentRepository.saveAll(createTestEquipment(locations));
            companyRepository.save(createTestCompany());
        };
    }

    private List<Equipment> createTestEquipment(List<Location> locations) {
        List<Equipment> equipmentList = new ArrayList<>();
        equipmentList.add(new Equipment(null, "Projector", "Ceiling-mounted HD projector", 1, locations.get(0)));
        equipmentList.add(new Equipment(null, "Whiteboard", "Large magnetic whiteboard", 2, locations.get(0)));
        equipmentList.add(new Equipment(null, "Laptop", "Windows 11 laptop for presentations", 3, locations.get(0)));
        equipmentList.add(new Equipment(null, "Conference Phone", "Polycom conference phone", 1, locations.get(1)));
        equipmentList.add(new Equipment(null, "Speaker System", "Bluetooth speaker system", 2, locations.get(1)));
        equipmentList.add(new Equipment(null, "Microphone", "Wireless handheld microphone", 4, locations.get(1)));
        equipmentList.add(new Equipment(null, "HDMI Cable", "2m HDMI cable", 5, locations.get(2)));
        equipmentList.add(new Equipment(null, "Laser Pointer", "Red laser pointer", 2, locations.get(2)));
        equipmentList.add(new Equipment(null, "Flip Chart", "Portable flip chart stand", 1, locations.get(3)));
        equipmentList.add(new Equipment(null, "Extension Cord", "5m power extension cord", 3, locations.get(4)));
        equipmentList.add(new Equipment(null, "Monitor", "27-inch 4K monitor", 2, locations.get(5)));
        equipmentList.add(new Equipment(null, "Tablet", "Android tablet for sign-in", 2, locations.get(5)));
        equipmentList.add(new Equipment(null, "Webcam", "HD USB webcam", 2, locations.get(6)));
        equipmentList.add(new Equipment(null, "Printer", "Laser printer", 1, locations.get(6)));
        equipmentList.add(new Equipment(null, "Scanner", "Document scanner", 1, locations.get(7)));
        equipmentList.add(new Equipment(null, "Router", "WiFi router", 1, locations.get(8)));
        equipmentList.add(new Equipment(null, "Coffee Machine", "Automatic coffee machine", 1, locations.get(9)));
        equipmentList.add(new Equipment(null, "Air Conditioner", "Portable air conditioner", 1, locations.get(9)));
        equipmentList.add(new Equipment(null, "Desk Lamp", "LED desk lamp", 6, locations.get(9)));
        equipmentList.add(new Equipment(null, "Chair", "Ergonomic office chair", 20, locations.get(10)));
        return equipmentList;
    }

    private List<Location> createTestLocations() {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(null, "Conference Room A", 20, new LocationStatusT(true, "Available"), new ArrayList<>() , new ArrayList<>()));
        locations.add(new Location(null, "Conference Room B", 15, new LocationStatusT(true, "Available"), new ArrayList<>(), new ArrayList<>()));
        locations.add(new Location(null, "Lecture Hall 1", 100, new LocationStatusT(true, "Available"), new ArrayList<>(), new ArrayList<>()));
        locations.add(new Location(null, "Lecture Hall 2", 80, new LocationStatusT(false, "Occupied"), new ArrayList<>(), new ArrayList<>()));
        locations.add(new Location(null, "Computer Lab 1", 30, new LocationStatusT(true, "Available"), new ArrayList<>(), new ArrayList<>()));
        locations.add(new Location(null, "Computer Lab 2", 25, new LocationStatusT(true, "Available"), new ArrayList<>(), new ArrayList<>()));
        locations.add(new Location(null, "Meeting Room 1", 10, new LocationStatusT(true, "Available"), new ArrayList<>(), new ArrayList<>()));
        locations.add(new Location(null, "Meeting Room 2", 12, new LocationStatusT(false, "Under Maintenance"), new ArrayList<>(), new ArrayList<>()));
        locations.add(new Location(null, "Breakout Room", 8, new LocationStatusT(true, "Available"), new ArrayList<>(), new ArrayList<>()));
        locations.add(new Location(null, "Library", 40, new LocationStatusT(true, "Available"), new ArrayList<>(), new ArrayList<>()));
        locations.add(new Location(null, "Workshop Room", 18, new LocationStatusT(true, "Available"), new ArrayList<>(), new ArrayList<>()));
        locations.add(new Location(null, "Seminar Room", 25, new LocationStatusT(true, "Available"), new ArrayList<>(), new ArrayList<>()));
        return locations;
    }

    private Company createTestCompany() {
        return new Company(null, "SkillHub Inc.", "skillhub@mail.com", "123-456-7890", LocalDate.now(), 
                            new AddressT("Skill Street","123","12345","New City"),
                            List.of(
                                new OpeningTimeT(DayET.MONDAY, LocalTime.of(10, 0), LocalTime.of(16, 0)),
                                new OpeningTimeT(DayET.TUESDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                                new OpeningTimeT(DayET.WEDNESDAY, LocalTime.of(12, 0), LocalTime.of(18, 0)),
                                new OpeningTimeT(DayET.THURSDAY, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                                new OpeningTimeT(DayET.FRIDAY, LocalTime.of(10, 0), LocalTime.of(16, 0))
                            ));
    }
}
