import { Component, inject } from '@angular/core';
import { AdminConfigComponent } from './admin-config/admin-config.component';
import { HasRoleDirective } from '../../../directives/HasRoleDirective';
import { UserConfigComponent } from "./user-config/user-config.component";
import { KeycloakService } from '../../../services/keycloak.service';
import { CommonModule } from '@angular/common';

/*
  * Die Konfigurationskomponente ermöglicht es Benutzern, ihre Einstellungen zu verwalten.
  * Abhängig von der Rolle des Benutzers wird entweder die Admin- oder die Benutzerkonfiguration angezeigt.
*/

@Component({
  selector: 'app-configuration',
  standalone: true,
  imports: [AdminConfigComponent, HasRoleDirective, UserConfigComponent, CommonModule],
  templateUrl: './configuration.component.html',
  styleUrl: './configuration.component.scss'
})
export class ConfigurationComponent {
  private readonly keycloakService = inject(KeycloakService);

  isAdmin() {
    // Überprüfen, ob der Benutzer die Rolle "admin" hat
    return this.keycloakService.keycloak.hasResourceRole("admin");
  }
}

