import { Component, inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { EquipmentModalComponent } from '../views/location-management/equipment-modal/equipment-modal.component';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';

/*
  * Dialog Komponente für Bestätigungsdialoge
  * Wird verwendet, um den Benutzer um Bestätigung für Aktionen wie Löschen zu bitten.
*/

@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
  imports: [CommonModule, MatButtonModule],
  templateUrl: './confirmation-dialog.component.html',
  styleUrl: './confirmation-dialog.component.scss'
})
export class ConfirmationDialogComponent implements OnInit {

  private readonly dialogRef = inject(MatDialogRef<EquipmentModalComponent>);
  private readonly data = inject(MAT_DIALOG_DATA);

  isDeleteConfirmation: boolean = false;
  isDeleteRegitrationConfirmation: boolean = false;

  ngOnInit(): void {
    if(this.data && this.data === 'deleteRegistration')
        this.isDeleteRegitrationConfirmation = true;
    else if (this.data && this.data === 'delete') {
        this.isDeleteConfirmation = true;
    }
  }

  onConfirm() {
    this.dialogRef.close(true);
  }

  onCancel() {
    this.dialogRef.close();
  }
}
