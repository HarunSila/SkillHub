// Angular core imports
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

// Angular Material imports
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

// Applikations spezifische imports
import { LocationManagementService } from '../../../services/api/location-managementApi.service';
import { Location } from '../../../models/entities/location';
import { FilterRequestDTO } from '../../../models/filterRequestDTO';
import { DayET } from '../../../models/dayET';
import { TimeRangeService } from '../../../services/timeRange.service';
import { CarouselComponent } from './carousel/carousel.component'; 
import { TimeslotSummaryComponent } from './timeslot-summary/timeslot-summary.component';
import { PictureUploadComponent } from './picture-upload/picture-upload.component';
import { PictureUploadService } from '../../../services/pictureUpload.service';
import { CoursService } from '../../../services/api/coursApi.service';
import { TimeRangeDTO } from '../../../models/timeRangeDTO';
import { ActivatedRoute, Router } from '@angular/router';
import { durationDivisibleBy30, hasSelectedLocations, isEndDateValid, isStartDateValid } from './cours-editor.validator';
import { createFilterRequestDTO, fetchImageAsFile, formDataAppender, mapLocationsToDaySlots, populateForm } from './cours.editor.helper';
import { Cours } from '../../../models/entities/cours';
import { Availability } from '../../../models/entities/availability';
import { KeycloakService } from '../../../services/keycloak.service';

@Component({
  selector: 'app-cours-editor',
  standalone: true,
  imports: [
    MatExpansionModule, FormsModule, ReactiveFormsModule, CommonModule, MatButtonModule, MatFormFieldModule,
    MatDatepickerModule, MatInputModule, MatIconModule, MatTabsModule, MatCardModule, MatSnackBarModule,
    CarouselComponent, TimeslotSummaryComponent, PictureUploadComponent
  ],
  templateUrl: './cours-editor.component.html',
  styleUrl: './cours-editor.component.scss'
})
export class CoursEditorComponent implements OnInit, OnDestroy {
  
    // --- Attribute ---
    availableLocations: Map<string, Map<DayET, TimeRangeDTO[]>> = new Map();
    allLocations: Location[] = [];

    // --- Injected Services ---
    private readonly cdr = inject(ChangeDetectorRef);
    private readonly formBuilder = inject(FormBuilder);
    private readonly timeRangeService = inject(TimeRangeService);
    private readonly pictureUploadService = inject(PictureUploadService);
    private readonly locationManagementService = inject(LocationManagementService);
    private readonly coursService = inject(CoursService);
    private readonly snackBar = inject(MatSnackBar);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly keycloakService = inject(KeycloakService);

    // --- Form Group ---
    courseForm = this.formBuilder.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      startDate: [new Date(), Validators.required],
      endDate: [new Date(), Validators.required],
      duration: [null, [Validators.required, Validators.min(30), durationDivisibleBy30]],
      maxParticipants: [null, [ Validators.required, Validators.min(1)]],
    });

    constructor() {
      this.courseForm.get('startDate')?.valueChanges.subscribe(() => {
        this.resetSelectionOnValueChange();
      });
      this.courseForm.get('endDate')?.valueChanges.subscribe(() => {
            this.resetSelectionOnValueChange();
      });
      this.courseForm.get('maxParticipants')?.valueChanges.subscribe(() => {
        this.resetSelectionOnValueChange();
      });
      this.courseForm.get('duration')?.valueChanges.subscribe(() => {
        this.availableLocations.clear();
        this.allLocations = [];
      });      
    }

    // Initialisiert den Kurs, wenn eine Kurs-ID in der URL vorhanden ist.
    // Ruft die Kursdetails ab und füllt das Formular mit den vorhandenen Daten.
    // Lädt die verfügbaren Orte und deren Zeitfenster für den Kurs.
    // Wenn Bilder für den Kurs vorhanden sind, werden diese geladen und im Bild-Upload-Service gespeichert.
    ngOnInit(): void {
      const id = this.route.snapshot.paramMap.get('id');
      if (id) {
        this.coursService.getCoursById(id).subscribe({
          next: (cours: Cours) => {
            populateForm(this.courseForm, cours);

            for (const a of cours.availabilities) {

              let availability: Availability = {
                location: a.location,
                weekday: DayET[a.weekday.toUpperCase() as keyof typeof DayET],
                startTime: a.startTime.slice(0, 5),
                endTime: a.endTime.slice(0, 5)
              }

              this.timeRangeService.addTimeRangeForDay(availability);
            }

            if (Array.isArray(cours.pictureUrls)) {
              this.pictureUploadService.clearImageFiles();
              console.log("pics removed")
              console.log(cours.pictureUrls);
              for (const url of cours.pictureUrls) {
                const fullUrl = this.coursService.providePictureUrl(url);
                fetchImageAsFile(fullUrl).then(file => {
                  this.pictureUploadService.addImageFile(file);
                });
              }
            }

            this.cdr.detectChanges();
          },
          error: (error) => {
            console.error('Error fetching course:', error);
            this.snackBar.open('Error fetching course. Please try again.', 'Close', { duration: 3000 });
          }
        });
      }
    }

    // Entfernt alle Bilder und Zeitfenster, wenn der Kurs-Editor verlassen wird.
    ngOnDestroy(): void {
      this.pictureUploadService.clearImageFiles();
      this.timeRangeService.clearTimeRanges();
    }

    // Setzt die verfügbaren Orte und Zeitfenster zurück, wenn sich das Startdatum, Enddatum oder die maximale Teilnehmer
    resetSelectionOnValueChange() {
      this.availableLocations.clear();
      this.allLocations = [];
      this.timeRangeService.clearTimeRanges();
    }

    // Filtert die verfügbaren Orte basierend auf den Kursformulardaten.
    // Erstellt ein FilterRequestDTO aus dem Kursformular und ruft den LocationManagementService auf, um die Orte zu filtern.
    // Aktualisiert die verfügbaren Orte mit den gefilterten Ergebnissen und löst eine Änderungserkennung aus.
    // Zeigt eine Fehlermeldung an, wenn ein Fehler beim Filtern der Orte auftritt.
    filterLocation() {
      const filterRequestDTO: FilterRequestDTO = createFilterRequestDTO(this.courseForm);
      
      this.locationManagementService.filterLocations(filterRequestDTO).subscribe({
        next: (filteredLocations: Map<string, Map<DayET, TimeRangeDTO[]>>) => {
          this.availableLocations = mapLocationsToDaySlots(filteredLocations);
          this.getLocations();
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error filtering locations:', error);
          this.snackBar.open('Error filtering locations. Please try again.', 'Close', {
            duration: 3000,
          });
        }
      });
    }

    // Erstellt Zeitbereiche für einen bestimmten Wochentag basierend auf den verfügbaren Zeitfenstern.
    createTimeRanges(timeSlots: Map<DayET, string[]>, day:DayET): string[] {
      return this.timeRangeService.createTimeRanges(timeSlots.get(day) || [], this.courseForm.get('duration')?.value ?? 0);
    }

    // Ruft alle verfügbaren Orte ab und aktualisiert die Liste der verfügbaren Orte.
    // Zeigt eine Fehlermeldung an, wenn ein Fehler beim Abrufen der Orte auftritt.
    getLocations() {
      this.locationManagementService.getLocations().subscribe({
        next: (locations: Location[]) => {
          this.allLocations = locations;
        },
        error: (error) => {
          console.error('Error fetching all locations:', error);
          this.snackBar.open('Error fetching locations. Please try again.', 'Close', {
            duration: 3000,
          });
        }
      });
    }

    // Konvertiert einen Schlüssel in einen DayET-Wert.
    getDayFromKey(key: string): DayET {
      return DayET[key as keyof typeof DayET];
    }

    // Gibt eine Map zurück, die die verfügbaren Zeitfenster für einen bestimmten Tag enthält.
    getAvailabilityPerDay(day: string) {
      let dayET: DayET = DayET[day as keyof typeof DayET];
      let availabilityMap: Map<Location, TimeRangeDTO[]> = new Map();
      this.availableLocations.forEach((timeSlots, locationId) => {
        if (timeSlots.has(dayET)) {
          availabilityMap.set(this.allLocations.find(loc => loc.id === locationId)!, timeSlots.get(dayET)!);
        }
      });
      return availabilityMap;
    }
  
    // Überprüft, ob für einen bestimmten Tag Zeitfenster verfügbar sind.
    showDaySlots(day: string){
      for (const timeSlots of this.availableLocations.values()) {
        if (timeSlots.has(DayET[day as keyof typeof DayET])) return true;
      }
      return false;
    }

    // Speichert den Kurs, wenn alle Validierungsbedingungen erfüllt sind.
    // Überprüft, ob die Dauer des Kurses gültig ist, ob das Start- und Enddatum korrekt sind und ob mindestens ein Standort ausgewählt wurde.
    // Erstellt ein FormData-Objekt mit den Kursdaten, Zeitbereichen und Bildern und sendet es an den CoursService.
    // Zeigt eine Erfolgsmeldung an, wenn der Kurs erfolgreich gespeichert wurde, oder eine Fehlermeldung bei einem Fehler.
    saveCourse() {
        const { duration, ...controls } = this.courseForm.controls;
        const allValidExceptDuration = Object.values(controls).every(control => control.valid);


      if (allValidExceptDuration && 
          isStartDateValid(this.snackBar, this.courseForm.get('startDate')?.value) && 
          isEndDateValid(this.snackBar, this.courseForm.get('startDate')?.value, this.courseForm.get('endDate')?.value) && 
          hasSelectedLocations(this.snackBar, this.timeRangeService.getTimeRanges())
        ) 
      {
        const formData = formDataAppender(
          this.timeRangeService.getTimeRanges(),
          this.courseForm,
          this.pictureUploadService.ImageFiles,
          this.keycloakService.profile.keycloakId,
          this.route.snapshot.paramMap.get('id') ?? undefined
        );

        this.coursService.saveCours(formData).subscribe({
          next: () => {
            this.clear();
            this.snackBar.open('Course saved successfully!', 'Close', { duration: 3000 });
          },
          error: (error) => {
            console.error('Error saving course:', error);
            this.snackBar.open('Error saving course. Please try again.', 'Close', { duration: 3000 });
          }
        });
      } 
    }

    // Löscht alle Bilder, setzt das Formular zurück, leert die verfügbaren Orte und Zeitbereiche
    // und navigiert zurück zum Kurs-Editor, wenn eine Kurs-ID in der URL vorhanden ist.
    clear() {
      this.pictureUploadService.clearImageFiles();
      this.courseForm.reset();
      this.availableLocations.clear();
      this.allLocations = [];
      this.timeRangeService.clearTimeRanges();
      this.cdr.detectChanges();
      if(this.route.snapshot.paramMap.get('id')) {
        this.router.navigate(['/cours-editor']);
      } 
    }
}
