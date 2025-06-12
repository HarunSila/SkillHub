import { ChangeDetectorRef, Component, inject, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { Cours } from '../../models/entities/cours';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { Availability } from '../../models/entities/availability';
import { CoursRegistration } from '../../models/entities/coursRegistration';
import { RegistrationStatusET, RegistrationStatusLabels } from '../../models/registrationStatusET';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';
import { DayET, DayOrder } from '../../models/dayET';
import { CarouselComponent } from './carousel/carousel.component';
import { CoursService } from '../../services/api/coursApi.service';
import { HasRoleDirective } from '../../directives/HasRoleDirective';
import { ProfileApiService } from '../../services/api/profileApi.service';
import { TrainerStatusET } from '../../models/trainerStatusET';
import { KeycloakService } from '../../services/keycloak.service';
import { CoursRegistrationApiService } from '../../services/api/coursRegistrationApi.service';

/*
 * Eine Komponente, die Kurskarten anzeigt
 * Zeigt eine Liste von Kursen an, die der Teilnehmer besucht hat oder besuchen kann.
 * Ermöglicht die Registrierung, Abmeldung und Anzeige von Kursdetails.
 * Die Kurse werden paginiert und können nach Bedarf bearbeitet oder gelöscht werden.
 * Die Komponente unterstützt auch die Anzeige von Kursbildern in einem Karussell.
*/

@Component({
  selector: 'app-cours-cards',
  standalone: true,
  imports: [
    MatCardModule, MatButtonModule, CommonModule, MatPaginatorModule, MatExpansionModule, 
    MatSnackBarModule, MatIconModule, CarouselComponent, HasRoleDirective
  ],
  templateUrl: './cours-cards.component.html',
  styleUrl: './cours-cards.component.scss'
})
export class CoursCardsComponent implements OnInit, OnChanges {
  @Input() participantCourses: Cours[] = [];
  @Input() showRegisterButton = false;
  
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);
  private readonly coursService = inject(CoursService);
  private readonly profileApiService = inject(ProfileApiService);
  private readonly keycloakService = inject(KeycloakService);
  private readonly coursRegistrationApiService = inject(CoursRegistrationApiService);

  pagedCourses: Cours[] = [];
  pageSize = 10;
  currentPage = 0;

  // Generelle Funktionalität zum Anzeigen von Kursen und Pagination
  ngOnInit(): void {
    this.updatePagedCourses();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['participantCourses']) {
      this.updatePagedCourses();
    }
  }

  onPageChange(event: PageEvent) {
    this.pageSize = event.pageSize;
    this.currentPage = event.pageIndex;
    this.updatePagedCourses();
  }

  updatePagedCourses() {
    const start = this.currentPage * this.pageSize;
    const end = start + this.pageSize;
    this.pagedCourses = this.participantCourses.slice(start, end);
    this.cdr.detectChanges();
  }

  // Für Teilnehmer wird der Registrierungsstatus des Kurses angezeigt
  getRegistrationStatus(cours: Cours) {
    const registrations = cours.registrations || [];
    const registration = registrations.find(r => r.participant?.keycloakId === this.keycloakService.profile.keycloakId);
    const status = registration ? registration.status : RegistrationStatusET.PENDING;
    return RegistrationStatusLabels[RegistrationStatusET[status as unknown as keyof typeof RegistrationStatusET]];
  }

  // Ordnet die Verfügbarkeiten der Kurse nach Wochentagen und gibt sie als HTML-String zurück, um sie in der Kurskarte anzuzeigen
  getScheduleHtml(availabilities: Availability[]) {
    const sortedAvailabilities = [...availabilities].sort((a, b) => {
        const indexA = DayOrder.indexOf(DayET[a.weekday as unknown as keyof typeof DayET]);
        const indexB = DayOrder.indexOf(DayET[b.weekday as unknown as keyof typeof DayET]);
      return indexA - indexB;
    });

    return sortedAvailabilities.map(a => {
      const dayDisplay = DayET[a.weekday as unknown as keyof typeof DayET] || a.weekday;
      const formatTime = (t: string) => t ? t.slice(0, 5) : '';
      return `${dayDisplay} ${formatTime(a.startTime)} - ${formatTime(a.endTime)}: ${a.location?.name}`;
    }).join('\n').replace(/\n/g, '<br>');
  }

  // Teilnehmer können sich für Kurse registrieren und die Registrierung wird im Backend gespeichert
  // Nach erfolgreicher Registrierung wird der Kurs aus der Liste der verfügbaren Kurse entfernt
  // und eine Erfolgsmeldung wird angezeigt.
  register(cours: Cours) {
    const coursRegistration: CoursRegistration = {
      registrationDate: new Date(),
      status: RegistrationStatusET.PENDING,
      cours: cours,
      participant: {
        keycloakId: this.keycloakService.profile.keycloakId,
        role: 'participant'
      }
    };

    this.coursRegistrationApiService.registerCours(coursRegistration).subscribe({
      next: (cours: any) => {
        this.participantCourses = this.participantCourses.filter(c => c.id !== cours.id);
        this.updatePagedCourses();
        this.snackBar.open('You have successfully registered for the course!', 'Close', {
          duration: 3000
        });
      }, error: (error: any) => {
        console.error('Error registering for course:', error);
        this.snackBar.open('Error registering for course', 'Close', {
          duration: 3000
        });
      }
    });
  }

  // Teilnehmer können sich von Kursen abmelden, wenn sie bereits registriert sind
  // Eine Bestätigungsnachricht wird angezeigt, bevor die Abmeldung durchgeführt wird.
  unregister(cours: Cours) {
    const registrations = cours.registrations || [];
    const registration = registrations.find(r => r.participant?.keycloakId === this.keycloakService.profile.keycloakId);
    if (!registration) {
      this.snackBar.open('Registration not found for this course.', 'Close', { duration: 3000 });
      return;
    }
    this.snackBar.open('Are you sure you want to unregister from this course?', 'Unregister', {duration: 5000})
    .onAction().subscribe(() => {
      this.coursRegistrationApiService.unregisterCours(registration.id!).subscribe({
        next: () => {
          this.snackBar.open('You have successfully unregistered from the course!', 'Close', {
            duration: 3000
          });
          this.participantCourses = this.participantCourses.filter(c => c.id !== cours.id);
          this.updatePagedCourses();
        },
        error: (error: any) => {
          console.error('Error unregistering from course:', error);
          this.snackBar.open('Error unregistering from course', 'Close', {
            duration: 3000
          });
        }
      });
    });
  }

  // Trainer können neue Kurse erstellen, indem sie zum Kurs-Editor navigieren
  // Vor dem Navigieren wird der Trainerstatus überprüft, um sicherzustellen, dass der Trainer aktiv ist.
  navigateToCoursEditor() {
    this.profileApiService.getTrainerStatus().subscribe({
      next: (status) => {
        if (status && TrainerStatusET[status as unknown as keyof typeof TrainerStatusET] === TrainerStatusET.AKTIV)
          this.router.navigate(['/cours-editor']);
        else
          this.snackBar.open('You profile is under review. After approval you can register new courses.', 'Close', { duration: 3000 });
      }, error: (error) => {
        console.error('Error fetching trainer status:', error);
        this.snackBar.open('Error checking trainer status', 'Close', { duration: 3000 });
      }
    });
  }

  // Trainer können Kurse bearbeiten, indem sie zum Kurs-Editor navigieren
  onEditCours(cours: Cours) {
    this.router.navigate(['/cours-editor', cours.id]);
  }

  // Trainer können Kurse löschen, indem sie eine Bestätigungsnachricht anzeigen
  // Beim Löschen wird der Kurs aus der Liste der Kurse entfernt und eine Erfolgsmeldung angezeigt.
  onDeleteCours(cours: Cours) {
    this.snackBar.open('Are you sure you want to delete this course?', 'Delete', {
      duration: 3000
    }).onAction().subscribe(() => {
      this.coursService.deleteCours(cours.id!).subscribe({
        next: () => {
          this.participantCourses = this.participantCourses.filter(c => c.id !== cours.id);
          this.updatePagedCourses();
          this.snackBar.open('Course deleted successfully!', 'Close', {
            duration: 3000
          });
        }, error: (error: any) => {
          console.error('Error deleting course:', error);
          this.snackBar.open('Error deleting course', 'Close', {
            duration: 3000
          });
        }
      });
    });
  }

  // Trainer können die Teilnehmer eines Kurses anzeigen, indem sie zur Teilnehmerliste navigieren
  onViewParticpants(cours: Cours) {
    this.router.navigate(['/cours-participants', cours.id]);
  }
}
