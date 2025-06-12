import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { KeycloakService } from "../keycloak.service";

/*
    * CoursService ermöglicht den Zugriff auf Kursdaten über HTTP-Anfragen.
    * Es bietet Methoden zum Abrufen aller Kurse, Abrufen von Kursen nach ID,
    * Speichern von Kursen, Löschen von Kursen und Bereitstellen von Bild-URLs.
    * Die Service-Methoden berücksichtigen die Rrolle des Benutzers.
*/

@Injectable({
    providedIn: 'root'
})
export class CoursService {
        private readonly httpClient = inject(HttpClient);
        private readonly backendUrl = environment.backend_url + '/cours';
        private readonly keycloakService = inject(KeycloakService);

        getAllCourses() {
            if (this.keycloakService.hasRoles('participant')) {
                const keycloakId: string = this.keycloakService.profile.keycloakId;
                return this.httpClient.post<any[]>(`${this.backendUrl}/getAllCourses`, keycloakId);
            } else return this.httpClient.get<any[]>(`${this.backendUrl}/getAllCourses`);
        }

        getCoursById(id: string) {
            return this.httpClient.get<any>(`${this.backendUrl}/getCoursById/${id}`);
        }

        getTrainerCourses() {
            const keycloakId: string = this.keycloakService.profile.keycloakId;
            return this.httpClient.post(`${this.backendUrl}/getTrainerCourses`, keycloakId);
        }

        getParticipantCourses() {
            const keycloakId: string = this.keycloakService.profile.keycloakId;
            return this.httpClient.post(`${this.backendUrl}/getParticipantCourses`, keycloakId);
        }

        saveCours(formData: FormData) {
            return this.httpClient.post(`${this.backendUrl}/saveCours`, formData);
        }

        providePictureUrl(pictureUrl: string) {
            return environment.backend_url + '/' + pictureUrl;
        }

        deleteCours(id: string) {
            return this.httpClient.delete(`${this.backendUrl}/deleteCours/${id}`);
        }
}