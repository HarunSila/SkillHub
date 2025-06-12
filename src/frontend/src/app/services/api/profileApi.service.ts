import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { UserAccount } from "../../models/entities/userAccount";
import { KeycloakService } from "../keycloak.service";
import { Trainer } from "../../models/entities/trainer";
import { Participant } from "../../models/entities/participant";
import { TrainerStatusET } from "../../models/trainerStatusET";

/*
    * ProfileApiService ermöglicht die Interaktion mit der Backend-API für Benutzerprofile.
    * Es bietet Methoden zum Abrufen, Aktualisieren und Löschen von Benutzerprofilen sowie zum Verwalten von Trainerstatus.
*/

@Injectable({
    providedIn: 'root'
})
export class ProfileApiService {
    private readonly httpClient = inject(HttpClient);
    private readonly backendUrl = environment.backend_url + '/profile';
    private readonly keycloakService = inject(KeycloakService);

    
    getAllUser() {
        return this.httpClient.get<any[]>(`${this.backendUrl}/getProfiles`);
    }

    getTrainerStatus() {
        const keycloakid = this.keycloakService.profile?.keycloakId;
        return this.httpClient.post<TrainerStatusET>(`${this.backendUrl}/getTrainerStatus`, keycloakid);
    }

    updateTrainerStatus(trainer: Trainer) {
        return this.httpClient.post(`${this.backendUrl}/updateTrainerStatus`, trainer);
    }

    saveProfile(user: UserAccount) {
        let userWithRole: Trainer | Participant;
        if (this.keycloakService.getRoles().includes('trainer'))
             userWithRole = user as Trainer;
        else 
            userWithRole = user as Participant;
        return this.httpClient.post(`${this.backendUrl}/saveProfile`, userWithRole);
    }

    deleteProfile(user: UserAccount) {
        let userWithRole: Trainer | Participant;
        if (this.keycloakService.getRoles().includes('trainer'))
             userWithRole = user as Trainer;
        else 
            userWithRole = user as Participant;
        return this.httpClient.post(`${this.backendUrl}/deleteProfile`, userWithRole);
    }

    deleteAsAdmin(user: UserAccount) {
        let userWithRole: Trainer | Participant;
        if (user.role === 'trainer')
             userWithRole = user as Trainer;
        else 
            userWithRole = user as Participant;
        return this.httpClient.post(`${this.backendUrl}/deleteProfile`, userWithRole);
    }
}