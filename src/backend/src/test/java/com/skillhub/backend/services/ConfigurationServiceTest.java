package com.skillhub.backend.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import com.skillhub.backend.models.entities.Company;
import com.skillhub.backend.repositories.CompanyRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigurationServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private ConfigurationService configurationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        configurationService = new ConfigurationService(companyRepository);
    }

    @Test
    void getCompany_returnsFirstCompany_whenCompaniesExist() {
        Company company = new Company();
        when(companyRepository.findAll()).thenReturn(List.of(company));
        Company result = configurationService.getCompany();
        assertEquals(company, result);
    }

    @Test
    void getCompany_throwsRuntimeException_whenNoCompaniesExist() {
        when(companyRepository.findAll()).thenReturn(Collections.emptyList());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> configurationService.getCompany());
        assertTrue(ex.getMessage().contains("No company found"));
    }

    @Test
    void getCompany_throwsRuntimeException_whenRepositoryThrows() {
        when(companyRepository.findAll()).thenThrow(new RuntimeException("DB error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> configurationService.getCompany());
        assertTrue(ex.getMessage().contains("Error fetching company"));
    }

    @Test
    void saveCompany_savesCompanySuccessfully() {
        Company company = new Company();
        when(companyRepository.save(company)).thenReturn(company);
        assertDoesNotThrow(() -> configurationService.saveCompany(company));
        verify(companyRepository, times(1)).save(company);
    }

    @Test
    void saveCompany_throwsRuntimeException_whenRepositoryThrows() {
        Company company = new Company();
        doThrow(new RuntimeException("DB error")).when(companyRepository).save(company);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> configurationService.saveCompany(company));
        assertTrue(ex.getMessage().contains("Error saving company"));
    }
}