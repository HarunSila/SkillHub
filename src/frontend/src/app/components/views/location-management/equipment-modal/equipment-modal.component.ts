import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Equipment } from '../../../../models/entities/equipment';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

/*
  * EquipmentModalComponent ermöglicht das Hinzufügen, Bearbeiten und Löschen von Ausrüstung.
  * Es enthält ein Formular zur Eingabe von Ausrüstungsdetails und verwendet den MatDialogRef, um die Modalität zu steuern.
*/

@Component({
  selector: 'app-equipment-modal',
  standalone: true,
  imports: [
    MatCardModule, CommonModule, ReactiveFormsModule, FormsModule, 
    MatButtonModule, MatFormFieldModule, MatInputModule
  ],
  templateUrl: './equipment-modal.component.html',
  styleUrl: './equipment-modal.component.scss'
})
export class EquipmentModalComponent implements OnInit {

  private readonly formBuilder = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<EquipmentModalComponent>);
  private readonly data = inject(MAT_DIALOG_DATA);

  showAddEquipment = false;
  showEditLocation = false;
  showDeleteLocation = false;

  equipmentForm = this.formBuilder.group({
    name: ['', [Validators.required]],
    description: ['', [Validators.required]],
    amount: [1, [Validators.required, Validators.min(1)]]
  });

  // Initialisiert das Formular basierend auf den übergebenen Daten
  ngOnInit(): void {
    if (this.data === 'add-equipment')
      this.showAddEquipment = true;
    else if (this.data === 'delete-location')
      this.showDeleteLocation = true;
  }

  // Setzt das Formular für die Bearbeitung eines Standorts
  save() {
    if (this.equipmentForm.valid) {
      const formValue = this.equipmentForm.getRawValue();
      const equipment: Equipment = {
        name: formValue.name as string,
        description: formValue.description as string,
        amount: formValue.amount as number
      };
      this.dialogRef.close(equipment);
    }
  }

  delete() {
    this.dialogRef.close('delete');
  }

  cancel() {
    this.dialogRef.close();
  }
}
