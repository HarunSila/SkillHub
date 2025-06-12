package com.skillhub.backend.models.entities;

// import java.beans.Transient; // Removed incorrect import
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity @Data @NoArgsConstructor @AllArgsConstructor
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "role"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Participant.class, name = "participant"),
    @JsonSubTypes.Type(value = Trainer.class, name = "trainer"),
    @JsonSubTypes.Type(value = Admin.class, name = "admin")
})
public abstract class UserAccount {
    @Id @GeneratedValue
    private UUID id;

    @Column(unique = true)
    private String keycloakId;

    @Transient
    private String role;

    @Transient
    private String name;

    @Transient
    private String surname;

    @Transient
    private String email;

    @Transient
    private String token;

    @Transient
    private String username;

    @Transient
    private String newPassword;

    @Transient
    private String confirmPassword;
}
