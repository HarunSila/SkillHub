import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, inject, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Equipment } from '../../../../models/entities/equipment';
import { EquipmentModalComponent } from '../equipment-modal/equipment-modal.component';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { LocationManagementService } from '../../../../services/api/location-managementApi.service';
import { Location } from '../../../../models/entities/location';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';

/*
 * AddLocationComponent ermöglicht das Hinzufügen und Bearbeiten von Standorten.
 * Es enthält ein Formular zur Eingabe von Standortdetails und eine Liste von zugehöriger Ausrüstung.
 * Die Komponente verwendet den LocationManagementService, um Standorte zu speichern und zu verwalten.
 */

@Component({
  selector: 'app-add-location',
  standalone: true,
  imports: [
    MatCardModule, FormsModule, ReactiveFormsModule, MatFormFieldModule, CommonModule,
    MatDialogModule, MatChipsModule, MatSlideToggleModule, MatSnackBarModule, MatButtonModule,
    MatInputModule, MatIconModule
  ],
  templateUrl: './add-location.component.html',
  styleUrl: './add-location.component.scss'
})
export class AddLocationComponent implements OnChanges{

  private readonly formBuilder = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly locationManagementService = inject(LocationManagementService);
  
  @Input() locations: Location[] = [];
  @Input() locationToEdit: Location | null = null;

  @Output() locationAdded = new EventEmitter<Location[]>();

  equipmentList: Equipment[] = [];

  locationForm = this.formBuilder.group({
    name: ['', Validators.required],
    capacity: ['', [Validators.required, Validators.min(1)]],
    status: this.formBuilder.group({
      active: [true],
      description: ['']
    })
  });

  // Initialisiert das Formular und setzt die Ausrüstungsliste zurück, wenn ein Standort zum Bearbeiten ausgewählt wird
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['locationToEdit'] && this.locationToEdit) {
      this.locationForm.patchValue({
        name: this.locationToEdit.name,
        capacity: this.locationToEdit.capacity?.toString() ?? '',
        status: {
          active: this.locationToEdit.status?.active ?? true,
          description: this.locationToEdit.status?.description ?? ''
        }
      });
      this.equipmentList = this.locationToEdit.equipmentList ? [...this.locationToEdit.equipmentList] : [];
      this.cdr.detectChanges();
    }
  }

  // Fügt ein neues Ausrüstungsobjekt hinzu, indem ein Dialog geöffnet wird
  addEquipment() {
      const dialogRef = this.dialog.open(EquipmentModalComponent, {
        width: '400px',
        data: 'add-equipment'
      });
  
      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.equipmentList.push(result);
          this.cdr.detectChanges();
        } else this.snackBar.open('Equipment couldn`t be  added', 'Close');
      });
    } 
  
    // Öffnet einen Dialog zur Bearbeitung eines vorhandenen Ausrüstungsobjekts
    removeEquipment(equipment: Equipment) {
      const index = this.equipmentList.indexOf(equipment);
      if (index >= 0) {
        this.equipmentList.splice(index, 1);
      }
    }
  
    // Speichert den Standort, wenn das Formular gültig ist
    // Aktualisiert die Liste der Standorte und gibt ein Event aus, wenn ein Standort hinzugefügt oder bearbeitet wurde
    // Zeigt eine Snackbar-Benachrichtigung an, wenn der Standort erfolgreich gespeichert wurde
    save() {
      if (this.locationForm.valid) {
        const location: Location = {
          name: this.locationForm.value.name as string,
          capacity: Number(this.locationForm.value.capacity),
          status: {
            active: this.locationForm.value.status?.active as boolean,
            description: this.locationForm.value.status?.description as string
          },
          equipmentList: this.equipmentList
        };

        if (this.locationToEdit) location.id = this.locationToEdit.id;
  
        this.locationManagementService.saveLocation(location).subscribe({
          next: (location) => {
            this.snackBar.open('Location saved successfully', 'Close', { duration: 2000 });
            if (this.locationToEdit) {
              const index = this.locations.findIndex(loc => loc.id === location.id);
              if (index !== -1) {
                this.locations[index] = location;
              }
            } else {
              this.locations = [...this.locations, location];
            }
            this.locationAdded.emit(this.locations);
            this.clearForm();
          },
          error: (error) => {
            console.error('Error saving location:', error);
            this.snackBar.open('Error saving location', 'Close');
          }
        });
      }
    }

    // Löscht den ausgewählten Standort, wenn er zum Bearbeiten ausgewählt wurde
    clearForm() {
      this.locationForm.reset();
      this.equipmentList = [];
      this.locationToEdit = null;
      this.cdr.detectChanges();
    }
}
