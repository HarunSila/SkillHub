package com.skillhub.backend.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Embeddable
public class AddressT {
    private String street;
    private String number;
    private String plz;
    private String city;
}
