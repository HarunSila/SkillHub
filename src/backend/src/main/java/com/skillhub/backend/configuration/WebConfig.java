package com.skillhub.backend.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration @EnableWebSecurity
public class WebConfig implements WebMvcConfigurer {

    private static final String ROLE_TRAINER = "trainer";
    private static final String ROLE_PARTICIPANT = "participant";
    private static final String ROLE_ADMIN = "admin";

    @Value("${spring.security.oauth2.client.provider.skillhub.issuer-uri}")
    private String issuerUri;

    // Konfiguration des Keycloak JWT-Authentifizierungskonverters und des JWT-Decoders
    @Bean
    KeycloakJwtAuthenticationConverter  keycloakJwtAuthenticationConverter() {
        return new KeycloakJwtAuthenticationConverter();
    }
    @Bean
    JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }

    // Konfiguration des Resource Handlers für statische Ressourcen
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }

    // CORS Konfiguration für die Entiwicklungsumgebung
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200")); // Erlaube Anfragen von der Angular-Anwendung
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE")); // Welche HTTP-Methoden sind erlaubt
        configuration.setAllowedHeaders(List.of("*")); // Erlaube alle Header
        configuration.setAllowCredentials(true); // Erlaube Cookies und Authentifizierungs-Header

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Registriere die CORS-Konfiguration für alle Endpunkte
        return source;
    }

    // Konfiguration der Sicherheitsfilterkette für die Anwendung
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authorize -> authorize
                // Erlaube den Zugriff auf die folgenden Endpunkte ohne Authentifizierung
                .requestMatchers("/", "/login", "/index.html", "/assets/**", "/static/**", "/favicon.ico", "/main.js", "/polyfills.js", "/uploads/**").permitAll()
                .requestMatchers("/*.js", "/chunk-*.js").permitAll()

                // Die folgenden Endpunkte sind für alle authentifizierten Benutzer zugänglich
                .requestMatchers(HttpMethod.GET, "/location-management/getLocations", "/cours/getAllCourses", "/configuration/getCompany",
                "/profile/saveProfile", "/profile/deleteProfile").authenticated()

                .requestMatchers(HttpMethod.POST, "/location-management/saveLocation").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/location-management/deleteLocation/{id}").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.POST, "/configuration/saveCompany").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.GET, "/user-management/getProfiles").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.POST, "/user-management/getTrainerStatus").hasRole(ROLE_ADMIN)
                .requestMatchers(HttpMethod.POST, "/user-management/updateTrainerStatus").hasRole(ROLE_ADMIN)

                .requestMatchers(HttpMethod.GET, "/location-management/filterLocations").hasRole(ROLE_TRAINER)
                .requestMatchers(HttpMethod.GET, "/cours/getCoursById/{id}").hasRole(ROLE_TRAINER)
                .requestMatchers(HttpMethod.POST, "/cours/saveCours").hasRole(ROLE_TRAINER)
                .requestMatchers(HttpMethod.POST, "/cours/getTrainerCourses").hasRole(ROLE_TRAINER)
                .requestMatchers(HttpMethod.POST, "cours-registration/assign-status").hasRole(ROLE_TRAINER)

                .requestMatchers(HttpMethod.DELETE, "/cours/deleteCours/{id}").hasAnyRole(ROLE_TRAINER, ROLE_ADMIN)

                .requestMatchers(HttpMethod.POST, "/cours-registration/register").hasRole(ROLE_PARTICIPANT)
                .requestMatchers(HttpMethod.POST, "/cours/getAllCourses").hasRole(ROLE_PARTICIPANT)
                .requestMatchers(HttpMethod.POST, "/cours/getParticipantCourses").hasRole(ROLE_PARTICIPANT)
                
                .requestMatchers(HttpMethod.DELETE, "/cours-registration/unregister/{id}").hasAnyRole(ROLE_PARTICIPANT, ROLE_TRAINER)
                
                .anyRequest().authenticated()) 
            .oauth2Login(oauth2Login -> oauth2Login.permitAll())

            .oauth2ResourceServer(server -> server
                .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter())));
        
        return http.build();
    }
}