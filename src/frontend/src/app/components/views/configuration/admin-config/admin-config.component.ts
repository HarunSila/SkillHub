import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ConfigurationService } from '../../../../services/api/configurationApi.service';
import { Company } from '../../../../models/entities/company';
import { DayET, DayOrder, getKeyByDayET } from '../../../../models/dayET';
import { OpeningTimeT } from '../../../../models/openingTimeT';
import { isEqual } from 'lodash';

/*
 * Die Admin-Konfiguration ermöglicht es Admins, die Konfiguration des Unternehmens zu verwalten.
 * Dies umfasst die Bearbeitung von Unternehmensinformationen und Öffnungszeiten.
*/

@Component({
  selector: 'app-admin-config',
  standalone: true,
  imports: [
      FormsModule, MatCardModule, MatButtonModule, MatInputModule, 
      MatFormFieldModule, CommonModule, ReactiveFormsModule, MatSnackBarModule,
      MatSelectModule, MatChipsModule, MatIconModule
  ],
  templateUrl: './admin-config.component.html',
  styleUrl: './admin-config.component.scss'
})
export class AdminConfigComponent implements OnInit{
    private readonly configurationService = inject(ConfigurationService);
    private readonly formBuilder = inject(FormBuilder);
    private readonly snackBar = inject(MatSnackBar);
    private readonly cdr = inject(ChangeDetectorRef);

    company!: Company;
    weekDays = [DayET.MONDAY, DayET.TUESDAY, DayET.WEDNESDAY, DayET.THURSDAY, DayET.FRIDAY, DayET.SATURDAY, DayET.SUNDAY];
    openingTimes: OpeningTimeT[] = [];

    companyForm: FormGroup = this.formBuilder.group({
    name: ['', Validators.required],
    contactPhone: ['', Validators.required],
    contactEmail: ['', [Validators.required, Validators.email]],
    address: this.formBuilder.group({
      street: ['', Validators.required],
      number: ['', Validators.required],
      plz: ['', Validators.required],
      city: ['', Validators.required],
    }),
  });

  openingTimeForm = this.formBuilder.group({
    weekday: [DayET.MONDAY, Validators.required],
    startTime: ['', Validators.required],
    endTime: ['', Validators.required],
  });

  // Initialisierung der Komponente
  // Lädt die Unternehmensdaten und initialisiert das Formular
  // sortiert die Öffnungszeiten nach Wochentagen
  ngOnInit(): void {
    this.configurationService.getCompany().subscribe({
      next: (company) => {
        this.company = company;
        this.openingTimes = this.company.openingTimes || [];
        this.openingTimes?.sort((a, b) =>
          DayOrder.indexOf(DayET[a.weekday.toUpperCase() as keyof typeof DayET]) -
          DayOrder.indexOf(DayET[b.weekday.toUpperCase() as keyof typeof DayET])
        );        
        this.initCompanyForm();
      }
    });
  }

  // Speichert die Unternehmensdaten und Öffnungszeiten
  // Überprüft, ob das Formular gültig ist und ob sich die Daten geändert haben
  // Zeigt eine Erfolgsmeldung an und lädt die Seite neu, wenn die Daten erfolgreich gespeichert wurden
  save() {
    if (this.companyForm.valid) {
      let company = this.companyForm.value;
      company.openingTimes = this.openingTimes;
      if(this.company && this.company.id) company.id = this.company.id;
      if(this.company && this.company.registrationDate) company.registrationDate = this.company.registrationDate;
      if(!this.objectsEqual(company, this.company)){
        company.registrationDate = new Date();
        this.configurationService.saveCompany(company).subscribe({
          next: () => {
            this.snackBar.open('Company data saved successfully!', 'Close', {
              duration: 10000
            });
            this.snackBar._openedSnackBarRef?.afterDismissed().subscribe(() => {
              window.location.reload();
            });
          },
          error: (error) => {
            console.error('Error saving company data:', error);
            this.snackBar.open('Error saving company data', 'Close', {
            });
          }
        });
      }
    }
  }

  // Fügt eine neue Öffnungszeit hinzu oder aktualisiert eine bestehende
  // Entfernt die Öffnungszeit, wenn sie bereits existiert
  // Sortiert die Öffnungszeiten nach Wochentagen und setzt das Formular zurück
  // Aktualisiert die Ansicht, um die Änderungen anzuzeigen
  addOpeningTime() {
    let openingTime = this.openingTimeForm.value;
    let key = getKeyByDayET(openingTime.weekday!) as DayET;
    openingTime.weekday = key;
    this.removeOpeningTime(openingTime as OpeningTimeT);
    this.openingTimes.push(openingTime as OpeningTimeT);
    this.openingTimes?.sort((a, b) =>
      DayOrder.indexOf(DayET[a.weekday.toUpperCase() as keyof typeof DayET]) -
      DayOrder.indexOf(DayET[b.weekday.toUpperCase() as keyof typeof DayET])
    );       
    this.openingTimeForm.reset();
    this.cdr.detectChanges();
  }

  // Entfernt eine Öffnungszeit basierend auf dem Wochentag
  // Aktualisiert die Ansicht, um die Änderungen anzuzeigen
  removeOpeningTime(ot: any) {
    this.openingTimes = this.openingTimes?.filter(o => o.weekday !== ot.weekday);
    this.cdr.detectChanges();
  }

  // Gibt die Öffnungszeit als String zurück, formatiert nach Wochentag und Uhrzeit
  getOpeningTimeDisplay(ot: OpeningTimeT): string {
    const dayDisplay = DayET[ot.weekday as unknown as keyof typeof DayET] || ot.weekday;
    const formatTime = (t: string) => t ? t.slice(0, 5) : '';
    return `${dayDisplay} ${formatTime(ot.startTime)} - ${formatTime(ot.endTime)}`;
  }

  // Vergleicht zwei Objekte, um zu überprüfen, ob sie gleich sind
  // Überprüft alle Eigenschaften der Objekte, um sicherzustellen, dass sie identisch sind
  private objectsEqual(obj1: any, obj2: any): boolean {
    return isEqual(obj1, obj2);
  }

  // Initialisiert das Formular für die Unternehmensdaten
  // Setzt die Standardwerte für die Felder und fügt Validierungen hinzu
  // Abonniert die Änderungen des Formulars, um den Zustand zu aktualisieren
  private initCompanyForm() {
    const company = this.company || {};
    const address = company.address! || {};

    this.companyForm = this.formBuilder.group({
      name: [company.name || '', Validators.required],
      contactPhone: [company.contactPhone || '', Validators.required],
      contactEmail: [company.contactEmail || '', [Validators.required, Validators.email]],
      address: this.formBuilder.group({
        street: [address.street || '', Validators.required],
        number: [address.number || '', Validators.required],
        plz: [address.plz || '', Validators.required],
        city: [address.city || '', Validators.required],
      }),
    });

    this.companyForm.valueChanges.subscribe(() => {
      if (this.companyForm.valid) {
        this.companyForm.markAsPristine();
      }
    });
  }
}
