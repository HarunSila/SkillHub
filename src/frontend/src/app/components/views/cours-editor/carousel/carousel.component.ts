import { ChangeDetectorRef, Component, inject, Input } from '@angular/core';
import { Location } from '../../../../models/entities/location';
import { MatCardModule } from '@angular/material/card';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatRadioModule } from '@angular/material/radio';
import { TimeRangeService } from '../../../../services/timeRange.service';
import { DayET } from '../../../../models/dayET';
import { FormsModule } from '@angular/forms';
import { Availability } from '../../../../models/entities/availability';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TimeRangeDTO } from '../../../../models/timeRangeDTO';

/*
 * Carousel-Komponente
 * Zeigt eine Karussell-ähnliche Ansicht von verfügbaren Standorten und deren Zeitfenstern an.
 * Ermöglicht die Auswahl eines Standorts und eines Zeitfensters, um Verfügbarkeiten hinzuzufügen.
 * Die Komponente unterstützt die Anzeige von Standortdetails wie Name, Beschreibung und Kapazität.
 * Die Zeitfenster werden aus den verfügbaren Zeitbereichen des Standorts generiert.
 * Die Komponente ermöglicht die Navigation zwischen den Standorten und das Hinzufügen von Verfügbarkeiten für einen bestimmten Wochentag.
 * Die Benutzer können durch die Standorte navigieren und für jeden Standort ein Zeitfenster auswählen.
*/

@Component({
  selector: 'app-carousel',
  standalone: true,
  imports: [
    MatCardModule, CommonModule, MatIconModule, MatButtonModule, MatTableModule,
    MatExpansionModule, MatRadioModule, FormsModule, MatSnackBarModule
  ],
  templateUrl: './carousel.component.html',
  styleUrl: './carousel.component.scss',
})
export class CarouselComponent {

  private readonly timeRangeService = inject(TimeRangeService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly snackBar = inject(MatSnackBar);

  @Input() availableLocations: Map<Location, TimeRangeDTO[]> = new Map();
  @Input() day!: DayET;

  selectedIndex = 0;
  selectedTimeSlot = '';

  displayedColumns = ['name', 'description', 'amount'];

  get availableLocationsArray() {
    return Array.from(this.availableLocations.entries());
  }


  // Setzt den ausgewählten Zeitbereich für den aktuellen Standort und Tag
  // Überprüft, ob der Zeitbereich gültig ist und zeigt eine Fehlermeldung an, wenn nicht
  addTimeRange(location: Location) {
    let availability: Availability = {
      location: location,
      weekday: this.day,
      startTime: this.selectedTimeSlot.split(' - ')[0],
      endTime: this.selectedTimeSlot.split(' - ')[1],
    }
    if(availability.location === undefined || availability.startTime === undefined || availability.endTime === undefined)
      this.snackBar.open('Please select a location and a time slot.', 'Close', { duration: 3000 });
    else {
      this.timeRangeService.addTimeRangeForDay(availability);
      this.cdr.detectChanges();
    }
  }

  // Navigation zwischen den Standorten
  previous() {
    if (this.selectedIndex > 0)
      this.selectedIndex--;
    else
      this.selectedIndex = this.availableLocationsArray.length - 1;
  }

  next() {
    if (this.selectedIndex < this.availableLocationsArray.length - 1)
      this.selectedIndex++;
    else
      this.selectedIndex = 0;
  }

  // Getter-Methoden für die Anzeige von Standortdetails
  getLocationEquipment(location: Location) {
    return location?.equipmentList.sort((a, b) => a.name.localeCompare(b.name)) || [];
  }

  getLocationName(location: Location): string {
    return location?.name || '';
  }

  getLocationCapacity(location: Location): string {
    return `${location?.capacity}` || '';
  }

  getLocationTimeslots(location: Location): string[] {
    const timeslots: string[] = [];
    if (!location || !this.availableLocations.has(location)) return [];
    else {
      for (const timeRange of this.availableLocations.get(location) || []) {
        const start = (timeRange.startTime || '').slice(0, 5);
        const end = (timeRange.endTime || '').slice(0, 5);
        timeslots.push(`${start} - ${end}`);
      }
      return timeslots;
    }
  }
}
