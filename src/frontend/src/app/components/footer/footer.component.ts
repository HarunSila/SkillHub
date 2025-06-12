import { Component, inject, OnInit } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { Company } from '../../models/entities/company';
import { ConfigurationService } from '../../services/api/configurationApi.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { DayET } from '../../models/dayET';
import { OpeningTimeT } from '../../models/openingTimeT';

/*
* Footer-Komponente
* Zeigt Informationen über das Unternehmen, Kontaktmöglichkeiten und Öffnungszeiten an.
* Stellt sicher, dass die Unternehmensdaten geladen werden und zeigt eine Fehlermeldung an, wenn dies fehlschlägt.
*/

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [MatIconModule, MatDividerModule, MatSnackBarModule, CommonModule],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss'
})
export class FooterComponent implements OnInit {
  private readonly configurationService = inject(ConfigurationService);
  snackBar = inject(MatSnackBar);
  company! : Company;

  ngOnInit(): void {
    this.configurationService.getCompany().subscribe({
      next: (company) => {
        this.company = company;
        if (this.company.openingTimes === undefined || this.company.openingTimes === null) {
          this.company.openingTimes = [];
        }
      },
      error: (error) => {
        console.error('Error fetching company data:', error);
        this.snackBar.open('Register your company under `Configuration`.', 'Close');
      }
    });
  }

  getPhoneNumber() {
    if (this.company && this.company.contactPhone) return this.company.contactPhone;
    else return null;
  }

  getEmail() {
    if (this.company && this.company.contactEmail) return this.company.contactEmail;
    else return null;
  }

  getYear() {
    if (this.company && this.company.registrationDate) {
      let date = new Date(this.company.registrationDate);
      return date.getFullYear();
    }
    else return null;
  }

  getName() {
    if (this.company && this.company.name) return this.company.name;
    else return null;
  }

  getStreet() {
    if (this.company && this.company.address!.street) return this.company.address!.street;
    else return null;
  }

  getNumber() {
    if (this.company && this.company.address!.number) return this.company.address!.number;
    else return null;
  }

  getPlz() {
    if (this.company && this.company.address!.plz) return this.company.address!.plz;
    else return null;
  }

  getCity() {
    if (this.company && this.company.address!.city) return this.company.address!.city;
    else return null;
  }

  getOpeningTimeDisplay(ot: OpeningTimeT): string {
    const dayDisplay = DayET[ot.weekday as unknown as keyof typeof DayET] || ot.weekday;
    const formatTime = (t: string) => t ? t.slice(0, 5) : '';
    return `${dayDisplay} ${formatTime(ot.startTime)} - ${formatTime(ot.endTime)}`;
  }
}
