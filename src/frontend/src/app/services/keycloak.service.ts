import { Injectable } from "@angular/core";
import Keycloak from "keycloak-js";
import { environment } from "../../environments/environment";
import { UserAccount } from "../models/entities/userAccount";

/*
    * KeycloakService ermöglicht die Authentifizierung und Autorisierung über Keycloak.
    * Es bietet Methoden zum Initialisieren von Keycloak, Überprüfen der Authentifizierung,
    * Abrufen von Benutzerprofilen und Rollen sowie zum Ein- und Ausloggen.
*/

@Injectable({
    providedIn: "root"
})
export class KeycloakService {

    private _keycloak!: Keycloak;
    private _profile!: UserAccount;

    // Getter für die Keycloak-Instanz
    get keycloak() {
        if (!this._keycloak) {
            this._keycloak = new Keycloak({
                url: environment.keycloak_url,
                realm: environment.realm,
                clientId: environment.clientId,
            });
        }
        return this._keycloak;
    }

    // Getter für das Benutzerprofil
    get profile() {
        return this._profile;
    }

    // Initialisierung der Keycloak-Instanz und Authentifizierung
    // Diese Methode sollte aufgerufen werden, um Keycloak zu initialisieren und den Benutzer zu authentifizieren
    // sie gibt ein Promise zurück, das aufgelöst wird, wenn die Initialisierung abgeschlossen ist
    async init() {
        let authenticated = this.keycloak?.authenticated;

        if(!authenticated){
            authenticated = await this.keycloak.init({
                onLoad: "login-required",   // Force login if not authenticated
            });
        }

        if(authenticated){
            let profile = await this.keycloak.loadUserProfile();
            this._profile = {
                role: this.keycloak?.tokenParsed?.resource_access?.[environment.clientId]?.roles[0] || "participant",
                keycloakId: profile.id || "",
                name: profile.firstName || "",
                surname: profile.lastName || "",
                username: profile.username || "",
                email: profile.email || "",
                token: this.keycloak?.token
            };
        }
    }

    // Überprüfen, ob der Benutzer angemeldet ist
    isLoggedIn() {
        return this.keycloak.authenticated;
    }

    // Abrufen der Rollen des Benutzers
    // Diese Methode gibt ein Array von Rollen zurück, die dem Benutzer zugeordnet sind
    getRoles() {
        return this.keycloak?.tokenParsed?.resource_access?.[environment.clientId]?.roles || [];
    }

    // Überprüfen, ob der Benutzer eine bestimmte Rolle hat
    hasRoles(role: string) {
        return this.getRoles().includes(role);
    }

    // Login Und Logout-Methoden
    login() {
        this.keycloak.login();
    }
    logout() {
        this.keycloak.logout();
    }
}