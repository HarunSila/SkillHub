import { Component, inject, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { Router } from '@angular/router';
import { HasRoleDirective } from '../../directives/HasRoleDirective';
import { KeycloakService } from '../../services/keycloak.service';

/*
  * Header-Komponente
  * Stellt die Navigationsleiste mit Logo, Links und Logout-Funktionalität bereit.
  * Ermöglicht die Navigation zu verschiedenen Seiten der Anwendung.
  */

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatMenuModule, HasRoleDirective],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent {
  @Input() logo = '';

  private readonly keycloakService = inject(KeycloakService);
  private readonly router = inject(Router);

  navigateTo(path: string) {
    this.router.navigate([path]);
  }

  async logout() {
    this.keycloakService.logout();
  }

  getUsername(): string {
    return this.keycloakService.profile?.username || '';
  }
}
