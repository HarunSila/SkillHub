import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { CoursRegistration } from "../../models/entities/coursRegistration";

/*
  * CoursRegistrationApiService ermöglicht die Interaktion mit der Backend-API für Kursanmeldungen.
  * Es bietet Methoden zum Registrieren, Abmelden und Zuweisen von Status für Kursanmeldungen.
*/

@Injectable({
  providedIn: 'root'
})
export class CoursRegistrationApiService {
  private readonly httpClient = inject(HttpClient);
  private readonly backendUrl = environment.backend_url + '/cours-registration';

  registerCours(coursRegistration: CoursRegistration) {
    return this.httpClient.post<CoursRegistration>(`${this.backendUrl}/register`, coursRegistration);
  }

  unregisterCours(coursRegistrationId: string) {
    return this.httpClient.delete(`${this.backendUrl}/unregister/${coursRegistrationId}`);
  }

  assignStatus(coursRegistration: CoursRegistration) {
    return this.httpClient.post<CoursRegistration>(`${this.backendUrl}/assign-status`, coursRegistration);
  }
}