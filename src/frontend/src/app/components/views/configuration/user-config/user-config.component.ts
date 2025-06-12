import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { KeycloakService } from '../../../../services/keycloak.service';
import { ProfileApiService } from '../../../../services/api/profileApi.service';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from '../../../confirmation-dialog/confirmation-dialog.component';

/*
 * UserConfigComponent ermöglicht es Benutzern, ihre Profile zu bearbeiten.
 * Benutzer können ihren Namen, Nachnamen, E-Mail und Passwort aktualisieren.
 * Es gibt auch die Möglichkeit, das Profil zu löschen.
 * Bei Änderungen wird eine Bestätigung angefordert, und es werden entsprechende Benachrichtigungen angezeigt.
*/

@Component({
  selector: 'app-user-config',
  standalone: true,
  imports: [
    FormsModule, MatCardModule, MatButtonModule, MatInputModule, 
    MatFormFieldModule, CommonModule, ReactiveFormsModule, MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: './user-config.component.html',
  styleUrl: './user-config.component.scss'
})
export class UserConfigComponent implements OnInit {

  private readonly formBuilder = inject(FormBuilder);
  private readonly keycloakService = inject(KeycloakService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly profileApiService = inject(ProfileApiService);
  private readonly dialog = inject(MatDialog);

  profileForm = this.formBuilder.group( {
    firstName: ['', [Validators.required, Validators.minLength(2)]],
    lastName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    newPassword: ['', [Validators.minLength(6)]],
    confirmPassword: ['', [Validators.minLength(6)]],
  })

  ngOnInit() {
    this.patchForm();
  }

  // Speichert das Profil des Benutzers
  // Überprüft, ob die neuen Passwörter übereinstimmen und aktualisiert das Profil
  // Zeigt eine Benachrichtigung an, wenn das Profil erfolgreich aktualisiert wurde
  saveProfile() {
    const formValue = this.profileForm.value;
    this.isConfirmed('save').subscribe((confirmed) => {
      if (confirmed) {
        if (formValue.newPassword && formValue.newPassword !== formValue.confirmPassword) {
          this.snackBar.open('New password and confirmation do not match.', 'Close', { duration: 3000 });
          return;
        } else {
          const profile = this.loadProfile(formValue);
          this.profileApiService.saveProfile(profile).subscribe({
            next: () => {
              this.snackBar.open('Profile updated successfully!', 'Close', { duration: 3000 });
              this.profileForm.reset();
              this.keycloakService.init();
              this.patchForm();
            },
            error: (error) => {
              this.snackBar.open(`Error updating profile: ${error.message}`, 'Close', { duration: 3000 });
            }
          });
        }
      }
    }); 
  }

  // Lädt das Profil mit den Werten aus dem Formular
  // Erstellt ein neues Profilobjekt mit den aktualisierten Werten
  loadProfile(formValue: any) {
    let profile = this.keycloakService.profile;
    profile.name = formValue.firstName!;
    profile.surname = formValue.lastName!;
    profile.email = formValue.email!;
    profile.newPassword = formValue.newPassword!;
    profile.confirmPassword = formValue.confirmPassword!;
    return profile;
  }

  // Füllt das Formular mit den aktuellen Profilinformationen des Benutzers
  patchForm() {
    this.profileForm.patchValue({
      firstName: this.keycloakService.profile.name,
      lastName: this.keycloakService.profile.surname,
      email: this.keycloakService.profile.email
    });
  }

  // Löscht das Profil des Benutzers
  // Zeigt eine Bestätigungsnachricht an, bevor das Profil gelöscht wird
  // Nach dem Löschen wird der Benutzer abgemeldet und eine Erfolgsmeldung angezeigt
  deleteProfile() {
    const profile = this.keycloakService.profile;
    this.isConfirmed('delete').subscribe((confirmed) => {
      if (confirmed) {
        this.profileApiService.deleteProfile(profile).subscribe({
          next: () => {
            this.snackBar.open('Profile deleted successfully!', 'Close', { duration: 3000 });
            this.keycloakService.logout();
          },
          error: (error) => {
            this.snackBar.open(`Error deleting profile: ${error.message}`, 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  // Öffnet einen Bestätigungsdialog mit der angegebenen Nachricht
  // Gibt ein Observable zurück, das nach dem Schließen des Dialogs aufgerufen wird
  isConfirmed(data: string) {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '400px',
      data: data
    });
    return dialogRef.afterClosed();
  }
}