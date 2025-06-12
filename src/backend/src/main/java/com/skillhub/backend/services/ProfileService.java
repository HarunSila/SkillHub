package com.skillhub.backend.services;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.skillhub.backend.models.TrainerStatusET;
import com.skillhub.backend.models.entities.Cours;
import com.skillhub.backend.models.entities.CoursRegistration;
import com.skillhub.backend.models.entities.Participant;
import com.skillhub.backend.models.entities.Trainer;
import com.skillhub.backend.models.entities.UserAccount;
import com.skillhub.backend.repositories.AvailabilityRepository;
import com.skillhub.backend.repositories.CoursRegistrationRepository;
import com.skillhub.backend.repositories.ParticipantRepository;
import com.skillhub.backend.repositories.TrainerRepository;
import com.skillhub.backend.repositories.CoursRepository;

@Service
public class ProfileService {
    
    @Value("${spring.security.oauth2.client.provider.skillhub.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;

    @Value("${frontend-client-uuid}")
    private String clientUuid;

    private WebClient webClient;

    private String adminToken;

    private final TrainerRepository trainerRepository;
    private final ParticipantRepository participantRepository;
    private final CoursRegistrationRepository coursRegistrationRepository;
    private final CoursRepository coursRepository;
    private final AvailabilityRepository availabilityRepository;

    ProfileService(
        TrainerRepository trainerRepository, 
        ParticipantRepository participantRepository,
        CoursRegistrationRepository coursRegistrationRepository,
        CoursRepository coursRepository,
        AvailabilityRepository availabilityRepository
    ) {
        this.trainerRepository = trainerRepository;
        this.participantRepository = participantRepository;
        this.coursRegistrationRepository = coursRegistrationRepository;
        this.coursRepository = coursRepository;
        this.availabilityRepository = availabilityRepository;
    }

    public UserAccount getUserPublicInfo(UserAccount userAccount) {
        try {
            this.initWebClient();
            JSONObject user = this.getUserFromKeycloak(userAccount.getKeycloakId());
            if (userAccount instanceof Trainer) {
                userAccount.setName(user.getString("firstName"));
                userAccount.setSurname(user.getString("lastName"));
            }
            userAccount.setUsername(user.getString("username"));
            userAccount.setEmail(user.getString("email"));
            return userAccount;
        } catch (Exception e) {
            System.err.println("Error fetching trainer public info: " + e.getMessage());
            throw new RuntimeException("Error fetching trainer public info", e);
        }
    }

    public List<UserAccount> getProfiles() {
        try {
            this.initWebClient();

            JSONArray users = getUsersFromKeycloak();
            List<JSONObject> usersWithRoles = getRolesFromKeycloak(users, clientUuid);
            
            //Entferne alle Nutzer die die Rolle "admin" haben
            List<UserAccount> filteredUsers = new ArrayList<>();
            for (JSONObject user : usersWithRoles) {
                if (!user.getJSONArray("clientRoles").toList().contains("admin")) {
                    UserAccount userAccount;
                    if(user.getJSONArray("clientRoles").toList().contains("trainer")) {
                        userAccount = new Trainer();
                        userAccount.setRole("trainer");
                        ((Trainer) userAccount).setStatus(
                            trainerRepository.getTrainerStatusByKeycloakId(user.getString("id"))
                            .orElse(null)
                        );
                    } else {
                        userAccount = new Participant();
                        if (user.getJSONArray("clientRoles").toList().contains("participant"))
                            userAccount.setRole("participant");
                    }
                    userAccount.setKeycloakId(user.getString("id"));
                    userAccount.setUsername(user.getString("username"));
                    userAccount.setName(user.getString("firstName"));
                    userAccount.setSurname(user.getString("lastName"));
                    userAccount.setEmail(user.getString("email"));
                    filteredUsers.add(userAccount);
                }
            }
            return filteredUsers;
        } catch (Exception e) {
            System.err.println("Error fetching profiles: " + e.getMessage());
            throw new RuntimeException("Error fetching profiles", e);
        }
    }

    public TrainerStatusET getTrainerStatus(String keycloakId) {
        try {
            return trainerRepository.getTrainerStatusByKeycloakId(keycloakId).orElse(null);
        } catch (Exception e) {
            System.err.println("Error fetching trainer status: " + e.getMessage());
            throw new RuntimeException("Error fetching trainer status", e);
        }
    }

    public void updateTrainerStatus(Trainer trainer) {
        try {
            Trainer existingTrainer = trainerRepository.findByKeycloakId(trainer.getKeycloakId()).orElse(null);
            if (existingTrainer != null) {
                existingTrainer.setStatus(trainer.getStatus());
                this.trainerRepository.save(existingTrainer);
            } else this.trainerRepository.save(trainer);
        } catch (Exception e) {
            System.err.println("Error updating trainer status: " + e.getMessage());
            throw new RuntimeException("Error updating trainer status", e);
        }
    }

    public void saveProfile(UserAccount userAccount) {
        try {
            this.initWebClient();
            updateUserAttributes(userAccount);
            if (userAccount.getNewPassword() != null && !userAccount.getNewPassword().isEmpty()) {
                updateUserPasswordToKeycloak(userAccount);
            }
        } catch (Exception e) {
            System.err.println("Error saving profile: " + e.getMessage());
            throw new RuntimeException("Error saving profile", e);
        }
    }

    public void deleteProfile(UserAccount userAccount) {
        try {
            this.initWebClient();
            
            if(userAccount instanceof Participant)
                this.deleteParticipantData(userAccount.getKeycloakId());
            else if(userAccount instanceof Trainer)
                this.deleteTrainerData(userAccount.getKeycloakId());

            this.deleteUserFromKeycloak(userAccount.getKeycloakId());
        } catch (Exception e) {
            System.err.println("Error deleting profile: " + e.getMessage());
            throw new RuntimeException("Error deleting profile", e);
        }
    }

    String getAdminAccessToken() {
        String tokenUrl = issuerUri.replace("/realms/", "/realms/") + "/protocol/openid-connect/token";
        String response = webClient.post()
            .uri(tokenUrl)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("grant_type=client_credentials"
                    + "&client_id=" + clientId
                    + "&client_secret=" + clientSecret)
            .retrieve()
            .bodyToMono(String.class)
            .block();
        // Extract the access_token from the response (use a JSON parser)
        return new JSONObject(response).getString("access_token");
    }

    JSONObject getUserFromKeycloak(String keycloakId) {
        String userUrl = issuerUri.replace("/realms/", "/admin/realms/") + "/users/" + keycloakId;
        String userResponse = webClient.get()
            .uri(userUrl)
            .header("Authorization", "Bearer " + adminToken)
            .retrieve()
            .bodyToMono(String.class)
            .block();
        return new JSONObject(userResponse);
    }

    JSONArray getUsersFromKeycloak() {
        String usersUrl = issuerUri.replace("/realms/", "/admin/realms/") + "/users";
        String usersResponse = webClient.get()
            .uri(usersUrl)
            .header("Authorization", "Bearer " + adminToken)
            .retrieve()
            .bodyToMono(String.class)
            .block();
        return new JSONArray(usersResponse);
    }

    List<JSONObject> getRolesFromKeycloak(JSONArray usersArray, String clientUuid) {
        List<JSONObject> usersWithRoles = new ArrayList<>();
        for (int i = 0; i < usersArray.length(); i++) {
            JSONObject user = usersArray.getJSONObject(i);
            String userId = user.getString("id");

            String userRolesUrl = issuerUri.replace("/realms/", "/admin/realms/") + "/users/" + userId + "/role-mappings/clients/" + clientUuid;
            String rolesResponse = webClient.get()
                .uri(userRolesUrl)
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            JSONArray rolesArray = new JSONArray(rolesResponse);

            List<String> roleNames = new ArrayList<>();
            for (int j = 0; j < rolesArray.length(); j++) {
                roleNames.add(rolesArray.getJSONObject(j).getString("name"));
            }
            user.put("clientRoles", roleNames);

            usersWithRoles.add(user);
        }
        return usersWithRoles;
    }

    void updateUserAttributes(UserAccount userAccount) {
        String userUrl = issuerUri.replace("/realms/", "/admin/realms/") + "/users/" + userAccount.getKeycloakId();

        webClient.put()
            .uri(userUrl)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{"
                + "\"firstName\":\"" + userAccount.getName() + "\","
                + "\"lastName\":\"" + userAccount.getSurname() + "\","
                + "\"email\":\"" + userAccount.getEmail() + "\""
                + "}")
            .retrieve()
            .toBodilessEntity()
            .block();
    }

    void updateUserPasswordToKeycloak(UserAccount userAccount) {
        String resetPasswordUrl = issuerUri.replace("/realms/", "/admin/realms/") + "/users/" + userAccount.getKeycloakId() + "/reset-password";

        webClient.put()
            .uri(resetPasswordUrl)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{"
                + "\"type\":\"password\","
                + "\"value\":\"" + userAccount.getNewPassword() + "\","
                + "\"temporary\":false"
                + "}")
            .retrieve()
            .toBodilessEntity()
            .block();
    }

    void deleteUserFromKeycloak(String keycloakId) {
        String userUrl = issuerUri.replace("/realms/", "/admin/realms/") + "/users/" + keycloakId;

        webClient.delete()
            .uri(userUrl)
            .header("Authorization", "Bearer " + adminToken)
            .retrieve()
            .toBodilessEntity()
            .block();
    }

    void deleteParticipantData(String keycloakId) {
        List<CoursRegistration> registrations = this.coursRegistrationRepository.findByParticipantKeycloakId(keycloakId);
        if (registrations != null && !registrations.isEmpty()) 
            this.coursRegistrationRepository.deleteAll(registrations);

        Participant participant = this.participantRepository.findByKeycloakId(keycloakId).orElse(null);
        if (participant != null)
            this.participantRepository.delete(participant);
    }

    void deleteTrainerData(String keycloakId) {
        List<Cours> courses = this.coursRepository.findAllWithAvailabilitiesByKeycloakId(keycloakId);
        if (courses != null && !courses.isEmpty()) {
            for (Cours cours : courses) {
                this.availabilityRepository.deleteAll(cours.getAvailabilities());
                this.coursRegistrationRepository.deleteAll(cours.getRegistrations());
                this.coursRepository.delete(cours);
            }
        }
        
        Trainer trainer = this.trainerRepository.findByKeycloakId(keycloakId).orElse(null);
        if (trainer != null)
            this.trainerRepository.delete(trainer);
    }

    void initWebClient() {
        webClient = WebClient.builder().build();
        adminToken = getAdminAccessToken();
    }
}