import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { Cours } from '../../../models/entities/cours';
import { CoursCardsComponent } from '../../cours-cards/cours-cards.component';
import { CoursService } from '../../../services/api/coursApi.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { KeycloakService } from '../../../services/keycloak.service';

/*
 * MyCoursesComponent zeigt die Kurse an, für die der Benutzer registriert ist.
 * Es unterscheidet zwischen Trainern und Teilnehmern und lädt die entsprechenden Kurse.
 * Bei einem Fehler beim Laden der Kurse wird eine Snackbar-Nachricht angezeigt.
 */

@Component({
  selector: 'app-my-courses',
  standalone: true,
  imports: [CoursCardsComponent, MatSnackBarModule],
  templateUrl: './my-courses.component.html',
  styleUrl: './my-courses.component.scss'
})
export class MyCoursesComponent implements OnInit {

  private readonly keycloakService = inject(KeycloakService);
  private readonly coursService = inject(CoursService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly snackBar = inject(MatSnackBar);

  registeredCourses: Cours[] = [];

  // Initialisiert die Komponente und lädt die Kurse basierend auf der Rolle des Benutzers
  ngOnInit(): void {
    if (this.keycloakService.profile.role === 'trainer') {
      this.loadTrainerCourses();
    } else if (this.keycloakService.profile.role === 'participant') {
      this.loadParticipantCourses();
    }
  }

  // Lädt die Kurse für Trainer oder Teilnehmer basierend auf der Rolle Trainer
  loadTrainerCourses(): void {
    this.coursService.getTrainerCourses().subscribe({
      next: (courses: any) => {
        this.registeredCourses = courses;
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.snackBar.open('Failed to load courses. Please try again later.', 'Close');
        console.error('Error fetching trainer courses:', error);
      }
    });
  }

  // Lädt die Kurse für Teilnehmer basierend auf der Rolle Teilnehmer
  loadParticipantCourses(): void {
    this.coursService.getParticipantCourses().subscribe({
      next: (courses: any) => {
        this.registeredCourses = courses;
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.snackBar.open('Failed to load courses. Please try again later.', 'Close');
        console.error('Error fetching participant courses:', error);
      }
    });
  }
}