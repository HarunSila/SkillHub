import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { ActivatedRoute } from '@angular/router';
import { CoursService } from '../../../services/api/coursApi.service';
import { Cours } from '../../../models/entities/cours';
import { RegistrationStatuses, RegistrationStatusET, RegistrationStatusLabels } from '../../../models/registrationStatusET';
import { ConfirmationDialogComponent } from '../../confirmation-dialog/confirmation-dialog.component';
import { CoursRegistration } from '../../../models/entities/coursRegistration';
import { CoursRegistrationApiService } from '../../../services/api/coursRegistrationApi.service';

/*
  * CoursParticipantsComponent zeigt die Teilnehmer eines Kurses an und ermöglicht es, deren Anmeldestatus zu ändern.
  * Trainer können den Status der Teilnehmer aktualisieren und Anmeldungen löschen.
  */

@Component({
  selector: 'app-cours-participants',
  standalone: true,
  imports: [
      MatTableModule, MatPaginatorModule, MatButtonModule, MatFormFieldModule, MatCardModule,
      FormsModule, ReactiveFormsModule, MatInputModule, MatSelectModule, CommonModule, MatDialogModule,
      MatSnackBarModule
    ],
  templateUrl: './cours-participants.component.html',
  styleUrl: './cours-participants.component.scss'
})
export class CoursParticipantsComponent implements OnInit {

  private readonly matDialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly route = inject(ActivatedRoute);
  private readonly coursService = inject(CoursService);
  private readonly coursRegistrationApiService = inject(CoursRegistrationApiService);
  displayedColumns: string[] = ['Username', 'Email', 'Status','Details']
  dataSource = new MatTableDataSource<any>([]);
  cours!: Cours;

  // Lädt die Kursdetails und Teilnehmer beim Initialisieren der Komponente
  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.coursService.getCoursById(id).subscribe({
        next: (cours: Cours) => {
          this.cours = cours;
          console.log(cours.registrations?.length)
          this.dataSource = new MatTableDataSource<any>(cours.registrations);
        }, error: (error) => {
          console.error('Error fetching course:', error);
          this.snackBar.open('Erreur lors de la récupération du cours', 'Fermer', {duration: 3000});
        }
      });   
    }
  }

  // Getter für die Anzeige der Registrierungsstatus
  getRegistrationStatuses() {
    return RegistrationStatuses;
  }

  getRegistrationStatusLabels(status: RegistrationStatusET) {
    return RegistrationStatusLabels[status];
  }

  // Filtert die Teilnehmerliste basierend auf der Eingabe im Filterfeld
  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  // Konvertiert den Registrierungsstatus von der Enum in einen lesbaren String
  getStatus(status: RegistrationStatusET) {
    return RegistrationStatusET[status as unknown as keyof typeof RegistrationStatusET];
  }

  // Weist den Teilnehmern einen neuen Status zu und aktualisiert die Anzeige
  // Überprüft, ob die maximale Teilnehmerzahl erreicht ist, bevor der Status geändert wird
  // Zeigt eine Bestätigungsdialog an, bevor der Status geändert wird
  // Aktualisiert den Status des Teilnehmers und zeigt eine Snackbar-Benachrichtigung an
  assignStatus(registration: CoursRegistration, status: RegistrationStatusET) {
    if(status === RegistrationStatusET.REGISTERED && this.cours.registrations!.length >= this.cours.maxParticipants) 
      this.snackBar.open('Maximum number of participants reached', 'Close', {duration: 3000})
    .afterDismissed().subscribe(() => location.reload());
    else {
      const matDialogRef = this.matDialog.open(ConfirmationDialogComponent, {
        data: 'save',
        width: '400px'
      });
      matDialogRef.afterClosed().subscribe((confirmed: boolean) => {
        if (confirmed) {
          registration.status = this.getStatus(status);
          this.subscribeStatusToService(registration, status);
        } else location.reload();
      });
    }
  }

  // Aktualisiert den Status des Teilnehmers im Backend und zeigt eine Snackbar-Benachrichtigung an
  // Bei Erfolg wird eine Erfolgsmeldung angezeigt, bei Fehlern eine Fehlermeldung
  subscribeStatusToService(registration: CoursRegistration, status: RegistrationStatusET) {
    this.coursRegistrationApiService.assignStatus(registration).subscribe({
      next: () => {
        this.snackBar.open(`Status updated to ${this.getRegistrationStatusLabels(status)} for user ${registration.participant!.username}`, 'Close', {
          duration: 3000
        })
      },
      error: (error: any) => {
        console.error(`Error updating status for user ${registration.participant!.username}:`, error);
        this.snackBar.open(`Error updating status for user ${registration.participant!.username}`, 'Close', {
          duration: 3000
        });
      }
    });
  }

  // Löscht die Registrierung eines Teilnehmers und aktualisiert die Anzeige
  // Zeigt eine Bestätigungsdialog an, bevor die Registrierung gelöscht wird
  // Bei Erfolg wird eine Erfolgsmeldung angezeigt, bei Fehlern eine Fehlermeldung
  deleteRegistration(registration: CoursRegistration) {
    const matDialogRef = this.matDialog.open(ConfirmationDialogComponent, {
      data: 'deleteRegistration',
      width: '400px'
    })
    matDialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.coursRegistrationApiService.unregisterCours(registration.id!).subscribe({
          next: () => {
            this.snackBar.open(`Registration for user ${registration.participant!.username} deleted`, 'Close', {
              duration: 3000
            });
            this.dataSource.data = this.dataSource.data.filter((reg: CoursRegistration) => reg.id !== registration.id);
          },
          error: (error: any) => {
            console.error(`Error deleting registration for user ${registration.participant!.username}:`, error);
            this.snackBar.open(`Error deleting registration for user ${registration.participant!.username}`, 'Close', {
              duration: 3000
            });
          }
        });
      }
    });
  }
}
