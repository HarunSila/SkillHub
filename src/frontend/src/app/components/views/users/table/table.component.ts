import { ChangeDetectorRef, Component, inject, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { TrainerStatusET } from '../../../../models/trainerStatusET';
import { MatSelectModule } from '@angular/material/select';
import { CommonModule } from '@angular/common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from '../../../confirmation-dialog/confirmation-dialog.component';
import { ProfileApiService } from '../../../../services/api/profileApi.service';
import { Trainer } from '../../../../models/entities/trainer';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

/*
  * TableComponent zeigt eine Tabelle mit Benutzerdaten an und ermöglicht es, den Status von Trainern zu ändern.
*/

@Component({
  selector: 'app-table',
  standalone: true,
  imports: [
    MatTableModule, MatPaginatorModule, MatButtonModule, MatFormFieldModule,
    FormsModule, ReactiveFormsModule, MatInputModule, MatSelectModule, CommonModule, MatDialogModule,
    MatSnackBarModule
  ],
  templateUrl: './table.component.html',
  styleUrl: './table.component.scss'
})
export class TableComponent implements OnInit, OnChanges {

  private readonly matDialog = inject(MatDialog)
  private readonly profileApiService = inject(ProfileApiService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly cdr = inject(ChangeDetectorRef);

  @Input() columns: string[] = [];
  @Input() data: any[] = [];
  @Input() role: string = '';

  dataSource = new MatTableDataSource<any>(this.data);

  trainerStatuses: TrainerStatusET[] = [
    TrainerStatusET.AKTIV,
    TrainerStatusET.BLOCKED,
    TrainerStatusET.PENDING
  ];

  trainerStatusLabels: { [key in TrainerStatusET]: string } = {
    [TrainerStatusET.AKTIV]: 'Active',
    [TrainerStatusET.BLOCKED]: 'Blocked',
    [TrainerStatusET.PENDING]: 'Pending'
  };
  
  ngOnInit(): void {
    this.dataSource.data = this.data;
  }

  ngOnChanges(changes: SimpleChanges): void {
      this.dataSource.data = this.data;
  }

  // Filtert die Tabelle basierend auf dem eingegebenen Suchbegriff
  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  // Liefert den Status eines Trainers als lesbaren Text
  getStatus(trainer: Trainer) {
    return TrainerStatusET[trainer.status as unknown as keyof typeof TrainerStatusET];
  }

  // Setzt den Status eines Trainers, wenn der Benutzer die Rolle 'trainer' hat
  // Zeigt eine Bestätigungsdialog an, bevor der Status geändert wird
  // Aktualisiert den Status des Trainers über den ProfileApiService
  // Zeigt eine Snackbar-Nachricht an, wenn der Status erfolgreich aktualisiert wurde oder ein Fehler aufgetreten ist
  assignStatus(trainer: Trainer, status: TrainerStatusET) {
    if (this.role === 'trainer') {
      const matDialogRef = this.matDialog.open(ConfirmationDialogComponent, {
        data: 'save',
        width: '400px'
      });
      matDialogRef.afterClosed().subscribe((confirmed: boolean) => {
        if (confirmed) {
          trainer.status = TrainerStatusET[status as unknown as keyof typeof TrainerStatusET];
          this.profileApiService.updateTrainerStatus(trainer).subscribe({
            next: () => {
              this.snackBar.open(`Status updated to ${this.trainerStatusLabels[status]} for user ${trainer.username}`, 'Close', {
                duration: 3000
              })
            },
            error: (error: any) => {
              console.error(`Error updating status for user ${trainer.username}:`, error);
              this.snackBar.open(`Error updating status for user ${trainer.username}`, 'Close', {
                duration: 3000
              });
            }
          });
        } else location.reload();
      });
    }
  }

  // Löscht einen Benutzer, wenn der Benutzer die Rolle 'admin' hat
  // Zeigt eine Bestätigungsdialog an, bevor der Benutzer gelöscht wird
  // Aktualisiert die Datenquelle, um den gelöschten Benutzer zu entfernen
  // Zeigt eine Snackbar-Nachricht an, wenn der Benutzer erfolgreich gelöscht wurde oder ein Fehler aufgetreten ist
  // Aktualisiert die Change Detection, um die Ansicht zu aktualisieren
  onDelete(user: any) {
    const matDialogRef = this.matDialog.open(ConfirmationDialogComponent, {
      data: 'delete',
      width: '400px'
    });
    matDialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.profileApiService.deleteAsAdmin(user).subscribe({
          next: () => {
            this.snackBar.open(`User ${user.username} deleted successfully`, 'Close', {
              duration: 3000
            });
            this.dataSource.data = this.dataSource.data.filter(u => u.id !== user.id);
            this.cdr.markForCheck();
          },
          error: (error: any) => {
            console.error(`Error deleting user ${user.username}:`, error);
            this.snackBar.open(`Error deleting user ${user.username}`, 'Close', {
              duration: 3000
            });
          }
        });
      }
    });
  }
}
