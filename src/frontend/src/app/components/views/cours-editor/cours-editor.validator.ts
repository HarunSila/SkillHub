import { AbstractControl, ValidationErrors } from "@angular/forms";
import { MatSnackBar } from "@angular/material/snack-bar";

/*
    * Validierungsfunktionen für den Kurs-Editor
    * Diese Funktionen überprüfen die Gültigkeit von Start- und Enddaten sowie die Auswahl von Standorten und Zeitfenstern.
    * Sie geben entsprechende Fehlermeldungen über das Snackbar-System aus, wenn die Validierung fehlschlägt.
*/

// Überprüft, ob das Startdatum gültig ist, d.h. ob es nicht in der Vergangenheit liegt.
export function isStartDateValid(snackBar: MatSnackBar, startDate: any): boolean {
    if (startDate) {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const start = new Date(startDate);
        start.setHours(0, 0, 0, 0);
        if (start < today) {
            snackBar.open('The start date cannot be in the past.', 'Close', { duration: 3000 });
            return false;
        } else return true;
    } else return false;
}

// Überprüft, ob das Enddatum gültig ist, d.h. ob es nicht vor dem Startdatum liegt.
export function isEndDateValid(snackBar: MatSnackBar, startDate: any, endDate: any) {
    if (startDate && endDate) {
        const start = new Date(startDate);
        const end = new Date(endDate);
        if (end < start) {
            snackBar.open('The end date cannot be before the start date.', 'Close', { duration: 3000 });
            return false;
        } else return true;
    } else return false;
}

// Überprüft, ob mindestens ein Standort und ein Zeitfenster ausgewählt sind.
export function hasSelectedLocations(snackBar: MatSnackBar, timeRanges: any[]) {
    if (timeRanges.length > 0) return true;
    else {
        snackBar.open('Please select at least one location and time slot.', 'Close', { duration: 3000 });
        return false;
    }
}

// Validiert, ob die Dauer eines Kurses durch 30 teilbar ist.
export function durationDivisibleBy30(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (value == null || value === '') return null; // Let required/min handle empty
    return value % 30 === 0 ? null : { notDivisibleBy30: true };
}