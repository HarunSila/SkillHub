import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { Company } from "../../models/entities/company";

/* 
    * ConfigurationService ermöglicht den Zugriff auf Konfigurationsdaten der Anwendung,
    * insbesondere die Unternehmensinformationen.
    * Es bietet Methoden zum Abrufen und Speichern von Unternehmensdaten über HTTP-Anfragen.
*/

@Injectable({
  providedIn: 'root'
})
export class ConfigurationService {
    private readonly httpClient = inject(HttpClient);
    private readonly backendUrl = environment.backend_url + '/configuration';

    getCompany() {
        return this.httpClient.get<Company>(`${this.backendUrl}/getCompany`);
    }

    saveCompany(company: Company) {
        return this.httpClient.post(`${this.backendUrl}/saveCompany`, company);
    }
}