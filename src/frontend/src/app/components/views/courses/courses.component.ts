import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { Cours } from '../../../models/entities/cours';
import { CoursCardsComponent } from '../../cours-cards/cours-cards.component';
import { CoursService } from '../../../services/api/coursApi.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { KeycloakService } from '../../../services/keycloak.service';
import { MatCardModule } from '@angular/material/card';
import { CommonModule } from '@angular/common';

/*
  * CoursesComponent zeigt eine Liste von Kursen an, die ein Teilnehmer besuchen kann.
  * Es verwendet den CoursService, um die Kurse zu laden und den KeycloakService, um die Benutzerrolle zu überprüfen.
  * Bei einem Fehler beim Laden der Kurse wird eine Snackbar-Nachricht angezeigt.
*/

@Component({
  selector: 'app-courses',
  standalone: true,
  imports: [CoursCardsComponent, MatSnackBarModule, MatCardModule, CommonModule],
  templateUrl: './courses.component.html',
  styleUrl: './courses.component.scss'
})
export class CoursesComponent implements OnInit {
  
  private readonly coursService = inject(CoursService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly keykloakService = inject(KeycloakService);
  participantCourses: Cours[] = [];

  // Lädt alle Kurse beim Initialisieren der Komponente
  ngOnInit() {
    this.coursService.getAllCourses().subscribe({
      next: (courses) => {
        this.participantCourses = courses;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error fetching courses:', error);
        this.snackBar.open('Failed to load courses. Please try again later.', 'Close');
      }
    });
  }

  // Überprüft, ob der Benutzer eine Rolle hat
  userHasRole(): boolean {
    return this.keykloakService.getRoles().length > 0;
  }
}
