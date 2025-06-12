import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { Location } from "../../models/entities/location";
import { Observable } from "rxjs";

/*
    * LocationManagementService ermöglicht die Verwaltung von Standorten.
    * Es bietet Methoden zum Abrufen, Speichern, Löschen und Filtern von Standorten über HTTP-Anfragen.
*/

@Injectable({
    providedIn: 'root'
})
export class LocationManagementService {
    private readonly httpClient = inject(HttpClient);
    private readonly backendUrl = environment.backend_url + '/location-management';

    getLocations() {
        return this.httpClient.get<Location[]>(`${this.backendUrl}/getLocations`);
    }

    saveLocation(location: Location) : Observable<any> {
        return this.httpClient.post(`${this.backendUrl}/saveLocation`, location);
    }

    deleteLocation(locationId: string) {
        return this.httpClient.delete(`${this.backendUrl}/deleteLocation/${locationId}`);
    }

    filterLocations(filterData: any): Observable<any> {
        return this.httpClient.post<any[]>(`${this.backendUrl}/filterLocations`, filterData);
    }
}