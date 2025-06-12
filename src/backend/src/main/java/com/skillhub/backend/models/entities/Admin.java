package com.skillhub.backend.models.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity @Data
@DiscriminatorValue("ADMIN") @EqualsAndHashCode(callSuper = true)
public class Admin extends UserAccount {
}
