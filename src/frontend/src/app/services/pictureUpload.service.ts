import { Injectable } from "@angular/core";
import { BehaviorSubject } from "rxjs";

/*
    * PictureUploadService ermöglicht das Hochladen, Entfernen und Verwalten von Bilddateien.
    * Es bietet Methoden zum Hinzufügen, Entfernen, Verschieben und Leeren von Bilddateien
    * sowie zum Abrufen der aktuellen Bilddateien.
    * Die Bilddateien werden als Observable bereitgestellt, um Änderungen zu verfolgen.
*/

@Injectable({
  providedIn: 'root'
})
export class PictureUploadService {
    // BehaviorSubject, um die Liste der Bilddateien zu verwalten
    private imageFilesSubject = new BehaviorSubject<{file: File, url: string, name: string }[]>([]);
    imageFiles$ = this.imageFilesSubject.asObservable(); 

    // Getter, um die aktuelle Liste der Bilddateien zu erhalten
    get ImageFiles() {
        return this.imageFilesSubject.value;
    }

    // Methode zum Hinzufügen einer Bilddatei
    addImageFile(file: File) {
        if (!file.type.startsWith('image/')) {
            return;
        }
        const reader = new FileReader();
        reader.onload = (e: any) => {
            this.imageFilesSubject.next([
                ...this.imageFilesSubject.value, 
                {file, url: e.target.result, name: file.name }
            ]);
        };
        reader.readAsDataURL(file);
    }

    // Methode zum Entfernen einer Bilddatei
    removeImageFile(index: number) {
        const files = [...this.imageFilesSubject.value];
        files.splice(index, 1);
        this.imageFilesSubject.next(files);
    }

    // Methode zum Leeren der Liste der Bilddateien
    clearImageFiles() {
        this.imageFilesSubject.next([]);
    }

    // Methode zum Verschieben einer Bilddatei innerhalb der Liste
    moveImageFile(fromIndex: number, toIndex: number) {
        const files = [...this.imageFilesSubject.value];
        if (
        fromIndex < 0 || fromIndex >= files.length ||
        toIndex < 0 || toIndex >= files.length ||
        fromIndex === toIndex
        ) {
            return;
        }
        const [moved] = files.splice(fromIndex, 1);
        files.splice(toIndex, 0, moved);
        this.imageFilesSubject.next(files);
    }
}