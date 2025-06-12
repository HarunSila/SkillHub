import { Injectable } from "@angular/core";
import { DayOrder } from "../models/dayET";
import { Availability } from "../models/entities/availability";
import { BehaviorSubject } from "rxjs";

/*
 * TimeRangeService bietet Funktionen zur Verwaltung von Zeitbereichen für verschiedene Wochentage.
 * Es ermöglicht das Erstellen von Zeitbereichen aus Zeit-Slots, das Hinzufügen und Entfernen von Verfügbarkeiten
 * sowie das Abrufen und Löschen von Zeitbereichen.
 * * Es verwendet einen BehaviorSubject, um die aktuellen Verfügbarkeiten zu speichern und zu verwalten,
 *   sodass Änderungen sofort in der Anwendung reflektiert werden können.
*/

@Injectable({
  providedIn: 'root'
})
export class TimeRangeService {

    // BehaviorSubject, um die Liste der Verfügbarkeiten zu verwalten
    private availabilitiesSubject = new BehaviorSubject<Availability[]>([]);
    availabilities$ = this.availabilitiesSubject.asObservable(); 
    
    
    /**
     * Generiert Zeitbereiche aus Zeit-Slots basierend auf einer angegebenen Dauer.
     * Jeder Slot muss genau 30 Minuten auseinander liegen, und die Funktion gibt Bereiche zurück, die der angegebenen Dauer entsprechen.
     * @param slotStrings - Ein Array von Zeit-Slots im "HH:mm" Format.
     * @param duration - Die gewünschte Dauer des Zeitbereichs in Minuten (z.B. 30, 60).
     * @return Ein Array von Zeitbereichen im "HH:mm - HH:mm" Format, die der angegebenen Dauer entsprechen.
     * * @Beispiel
     * createTimeRanges(["08:00", "08:30", "09:00", "09:30"], 60);
     * // Returns: ["08:00 - 09:00", "08:30 - 09:30"]
     * */
    createTimeRanges(slotStrings: string[], duration: number): string[] {
        if (slotStrings.length === 0 || !duration || duration <= 0) return [];
        const timeRangeStrings: string[] = [];

        // Helfer um format "HH:mm" zu Minuten zu konvertieren
        const toMinutes = (time: string) => {
            const [h, m] = time.split(':').map(Number);
            return h * 60 + m;
        };

        // Helfer um Minuten in format "HH:mm" zu konvertieren
        const toTimeString = (minutes: number) => {
            const h = Math.floor(minutes / 60).toString().padStart(2, '0');
            const m = (minutes % 60).toString().padStart(2, '0');
            return `${h}:${m}`;
        };

        // Konvertiere die Slot-Zeichenketten in Minuten
        const slotMinutes = slotStrings.map(toMinutes);

        for (let i = 0; i < slotMinutes.length; i++) {
            let start = slotMinutes[i];
            let current = start;
            let count = 1;

            // Wenn die Dauer 30 Minuten ist, füge den Slot direkt hinzu
            if (duration === 30) {
                timeRangeStrings.push(`${toTimeString(start)} - ${toTimeString(start + 30)}`);
                continue; // Skip to the next slot
            }

            // Iteriere über die folgenden Slots, um den Bereich zu erweitern
            for (let j = i + 1; j < slotMinutes.length; j++) {
                // Prüfe, ob der aktuelle Slot 30 Minuten nach dem Start liegt
                if (slotMinutes[j] - current === 30) {
                    current = slotMinutes[j];
                    count++;
                    // Wenn die Anzahl der Slots die gewünschte Dauer erreicht, füge den Bereich hinzu
                    if ((current - start) + 30 === duration) {
                        timeRangeStrings.push(`${toTimeString(start)} - ${toTimeString(current + 30)}`);
                        break;
                    }
                } else {
                    break; // Wenn der Slot nicht 30 Minuten auseinander liegt, breche die Schleife ab
                }
            }
        }

        // Entferne Duplikate und sortiere die Zeitbereiche
        return Array.from(new Set(timeRangeStrings));
    }

    // Fügt einen neuen Zeitbereich für einen bestimmten Wochentag hinzu oder aktualisiert ihn, wenn er bereits existiert
    addTimeRangeForDay(availability: Availability) {
        this.removeTimeRangeForDay(availability);
        const newAvailabilities = [...this.availabilitiesSubject.value, availability];
        newAvailabilities.sort((a, b) => DayOrder.indexOf(a.weekday) - DayOrder.indexOf(b.weekday));
        this.availabilitiesSubject.next(newAvailabilities);
    }

    // Entfernt einen Zeitbereich für einen bestimmten Wochentag
    removeTimeRangeForDay(availability: Availability) {
        this.availabilitiesSubject.next(
            this.availabilitiesSubject.value.filter(a => a.weekday !== availability.weekday)
        );
    }

    // Entfernt einen Zeitbereich für einen bestimmten Wochentag und eine bestimmte Zeit
    getTimeRanges() {
        return this.availabilitiesSubject.value;
    }

    // Gibt die aktuellen Verfügbarkeiten zurück
    clearTimeRanges() {
        this.availabilitiesSubject.next([]);
    }
}