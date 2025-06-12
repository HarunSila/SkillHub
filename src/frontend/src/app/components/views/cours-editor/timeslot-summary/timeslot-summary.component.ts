import { Component, inject } from '@angular/core';
import { TimeRangeService } from '../../../../services/timeRange.service';
import { Availability } from '../../../../models/entities/availability';
import { MatChipsModule } from '@angular/material/chips';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';
import { MatIconModule } from '@angular/material/icon';

/*
  * TimeslotSummaryComponent zeigt eine Zusammenfassung der verfügbaren Zeitfenster an.
  * Es ermöglicht das Entfernen von Verfügbarkeiten für einen bestimmten Tag.
  * Die Komponente verwendet den TimeRangeService, um die verfügbaren Zeitfenster zu verwalten.
  */

@Component({
  selector: 'app-timeslot-summary',
  standalone: true,
  imports: [MatChipsModule, CommonModule, MatIconModule],
  templateUrl: './timeslot-summary.component.html',
  styleUrl: './timeslot-summary.component.scss'
})
export class TimeslotSummaryComponent {
  private readonly timeRangeService = inject(TimeRangeService);

  // Observable, das die verfügbaren Zeitfenster für den aktuellen Tag bereitstellt
  availabilities$: Observable<Availability[]> = this.timeRangeService.availabilities$;

  // Entfernt eine Verfügbarkeit für einen bestimmten Tag
  removeAvailability(availability: Availability) {
    this.timeRangeService.removeTimeRangeForDay(availability);
  }
}
