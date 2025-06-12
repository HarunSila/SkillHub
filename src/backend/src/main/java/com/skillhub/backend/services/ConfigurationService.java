package com.skillhub.backend.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.skillhub.backend.models.entities.Company;
import com.skillhub.backend.repositories.CompanyRepository;

@Service
public class ConfigurationService {
    
    private final CompanyRepository companyRepository;

    ConfigurationService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company getCompany() {
        try {
            List<Company> companies = companyRepository.findAll();
            if (companies.isEmpty()) {
                System.out.println("No company found");
                throw new RuntimeException("No company found");
            }
            return companies.get(0);
        } catch (Exception e) {
            System.err.println("Error fetching company: " + e.getMessage());
            throw new RuntimeException("Error fetching company: " + e.getMessage(), e);
        }
    }

    public void saveCompany(Company company) {
        try {
            companyRepository.save(company);
        } catch (Exception e) {
            System.err.println("Error saving company: " + e.getMessage());
            throw new RuntimeException("Error saving company: " + e.getMessage(), e);
        }
        
    }
}
