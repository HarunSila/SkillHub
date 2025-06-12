package com.skillhub.backend.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillhub.backend.models.entities.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
}
