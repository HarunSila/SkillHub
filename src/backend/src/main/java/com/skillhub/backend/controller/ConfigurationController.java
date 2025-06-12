package com.skillhub.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillhub.backend.models.entities.Company;
import com.skillhub.backend.services.ConfigurationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController @RequestMapping("/configuration") @CrossOrigin(origins = "http://localhost:4200")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping("/getCompany")
    public ResponseEntity<Company> getCompany() {
        try {
            Company company = this.configurationService.getCompany();
            return ResponseEntity.ok(company);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @PostMapping("/saveCompany")
    public ResponseEntity<Void> saveCompany(@RequestBody Company company) {
        try{
            this.configurationService.saveCompany(company);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
}
