import { DayET, getKeyByDayET } from "../../../models/dayET";
import { Availability } from "../../../models/entities/availability";
import { Cours } from "../../../models/entities/cours";
import { FilterRequestDTO } from "../../../models/filterRequestDTO";
import { TimeRangeDTO } from "../../../models/timeRangeDTO";

/*
    * Hilfsfunktionen für den Cours-Editor
    * Diese Funktionen helfen bei der Formatierung von Daten, der Erstellung von FormData-Objekten
    * und der Verarbeitung von Kursinformationen.
*/

// Formatieren eines Datums in das lokale ISO-Format (YYYY-MM-DDT00:00:00.000Z)
export function formatDateToLocalISO(date: Date): string {
    // Returns YYYY-MM-DDT00:00:00.000Z for the local date
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}T00:00:00.000Z`;
}

// Konvertiert eine Map von Standorten und deren Zeitfenstern in eine Map, die die Tage als Schlüssel verwendet
export function mapLocationsToDaySlots(filteredLocations: Map<String, Map<DayET, TimeRangeDTO[]>>) {
    return new Map(
        Object.entries(filteredLocations).map(([locationId, dayMap]) => [
            locationId,
            new Map(
            Object.entries(dayMap).map(([dayKey, slots]) => [
                DayET[dayKey as keyof typeof DayET],
                slots as TimeRangeDTO[]
            ])
            )
        ])
    );
}

// Erstellt ein FormData-Objekt für die Kursinformationen, einschließlich Verfügbarkeiten und Bilder
export function formDataAppender(
        availabilities: Availability[], 
        coursForm: any, 
        imageFiles: {file: File, url: string, name: string }[],
        keycloakId: string,
        id?: string,
    ) 
{
    const formData = new FormData();
    
    availabilities.forEach(av => av.weekday = getKeyByDayET(av.weekday) as DayET);

    let startDate = formatDateToLocalISO(new Date(new Date(coursForm.get('startDate')?.value!)));
    let endDate = formatDateToLocalISO(new Date(coursForm.get('endDate')?.value!));

    if (id) formData.append('id', id);
    formData.append('title', coursForm.get('title')?.value!);
    formData.append('description', coursForm.get('description')?.value!);
    formData.append('startDate', startDate);
    formData.append('endDate', endDate);
    formData.append('maxParticipants', coursForm.get('maxParticipants')?.value!);
    formData.append('availability', JSON.stringify(availabilities));
    imageFiles.forEach(file => {
        formData.append('pictures', file.file, file.name);
    });

    formData.append('keycloakId', keycloakId);

    return formData;
}

// Erstellt ein FilterRequestDTO aus dem Kursformular
export function createFilterRequestDTO(coursForm: any) {
    const startDate = coursForm.get('startDate')?.value;
    const endDate = coursForm.get('endDate')?.value;

    const filterRequestDTO: FilterRequestDTO = {
        startDate: startDate ? formatDateToLocalISO(new Date(startDate)) : null,
        endDate: endDate ? formatDateToLocalISO(new Date(endDate)) : null,
        maxParticipants: coursForm.get('maxParticipants')?.value || null,
        duration: coursForm.get('duration')?.value || null
    };

    return filterRequestDTO;
}

// Füllt ein Kursformular mit den Werten eines Cours-Objekts
export function populateForm(coursForm: any, cours: Cours) {
    coursForm.patchValue({
        title: cours.title,
        description: cours.description,
        startDate: new Date(cours.startDate),
        endDate: new Date(cours.endDate),
        maxParticipants: cours.maxParticipants
    });
}

// Lädt ein Bild von einer URL und gibt es als File-Objekt zurück
export async function fetchImageAsFile(url: string) {
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const blob = await response.blob();
        const fileName = url.split('/').pop() || 'image.jpg';
        return new File([blob], fileName, { type: blob.type });
    } catch (error) {
        console.error('Error fetching image:', error);
        throw error;
    }
}