package com.skillhub.backend.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/*
 * Diese Klasse ist für die Konvertierung des JWT-Tokens in ein AbstractAuthenticationToken zuständig.
 * Hierbei wird der JWT-Token in ein JwtAuthenticationToken konvertiert und die Rollen des Benutzers
 * aus dem Token extrahiert und in das JwtAuthenticationToken übertragen.
 * 
 * Es ist notwendig, dass die Rollen des Benutzers im JWT-Token unter dem Key "resource_access" und
 * dem Key "skillhub-frontend-client" gespeichert sind. Die Rollen müssen als Liste von Strings
 * unter dem Key "roles" gespeichert sein.
 * 
 * Da Keycloak die Rollen mit Bindestrichen speichert, werden diese in Unterstriche umgewandelt und
 * mit dem Präfix "ROLE_" versehen entsprechend dem Spring Security Standard.
 */

public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    // Konvertiert den JWT-Token in ein JwtAuthenticationToken und fügt die Rollen des Benutzers hinzu
    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        return new JwtAuthenticationToken(
            jwt,
            Stream.concat(
                new JwtGrantedAuthoritiesConverter().convert(jwt).stream(),
                extractResourceAccessRoles(jwt).stream())
                .collect(Collectors.toSet()));
            
    }

    // Extrahiert die Rollen des Benutzers aus dem JWT-Token und konvertiert sie in GrantedAuthorities
    private Collection<? extends GrantedAuthority> extractResourceAccessRoles(Jwt jwt) {
        var jwtClaim = jwt.getClaim("resource_access");

        if (jwtClaim == null || !(jwtClaim instanceof Map)) return Set.of();  

        @SuppressWarnings("unchecked")
        var resourceAccess = (Map<String, List<String>>) jwtClaim;

        // Die Rollen müssen unter dem Key "resource_access" und dem Key "rechnungsapp-frontend-client" gespeichert sein
        if (!resourceAccess.containsKey("skillhub-frontend-client") || !(resourceAccess instanceof Map)) return Set.of();

        @SuppressWarnings("unchecked")
        var skillhub = (Map<String, List<String>>) resourceAccess.get("skillhub-frontend-client");
        
        // Die Rollen müssen als Liste von Strings unter dem Key "roles" gespeichert sein
        var roles = (ArrayList<String>) skillhub.get("roles");

        // Die Rollen werden mit dem Präfix "ROLE_" versehen und Bindestriche durch Unterstriche ersetzt
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.replace("-","_")))
            .collect(Collectors.toSet());
    }
}
