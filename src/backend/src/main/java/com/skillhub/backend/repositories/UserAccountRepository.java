package com.skillhub.backend.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillhub.backend.models.entities.UserAccount;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
}
