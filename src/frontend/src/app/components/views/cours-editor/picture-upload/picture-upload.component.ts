import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { PictureUploadService } from '../../../../services/pictureUpload.service';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Observable } from 'rxjs';

/*
 * PictureUploadComponent ermöglicht das Hochladen und Verwalten von Bildern.
 * Trainer können Bilder per Drag & Drop oder über eine Dateiauswahl hochladen.
 * Die Bilder werden in einer Liste angezeigt, die es ermöglicht, sie zu entfernen oder ihre Reihenfolge zu ändern.
 */

@Component({
  selector: 'app-picture-upload',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule],
  templateUrl: './picture-upload.component.html',
  styleUrl: './picture-upload.component.scss'
})
export class PictureUploadComponent {

  private readonly pictureUploadService = inject(PictureUploadService);
  private dragSrcIndex: number | null = null;

  imageFiles$: Observable<{ file: File, url: string, name: string }[]> = this.pictureUploadService.imageFiles$;

  // Nimmt eine Datei oder mehrere über ein Dateieingabefeld entgegen und fügt sie dem Bild-Upload-Service hinzu.
  onFileSelected(event: Event) {
    const files = (event.target as HTMLInputElement).files;
    if (files) {
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        this.pictureUploadService.addImageFile(file);
      }
    }
  }

  // Entfernt ein Bild anhand des Indexes aus dem Bild-Upload-Service.
  removeImage(index: number) {
    this.pictureUploadService.removeImageFile(index);
  }

  // Ermöglicht das Drag & Drop von Bildern, um ihre Reihenfolge zu ändern.
  onDragStart(index: number) {
    this.dragSrcIndex = index;
  }

  // Verhindert das Standardverhalten beim Ziehen über ein anderes Bild, um die Reihenfolge zu ändern.
  onDragOver(event: DragEvent, index: number) {
    event.preventDefault();
  }

  // Wenn ein Bild fallen gelassen wird, wird die Reihenfolge im Bild-Upload-Service aktualisiert.
  onDrop(index: number) {
    if (this.dragSrcIndex === null || this.dragSrcIndex === index) return;
    this.pictureUploadService.moveImageFile(this.dragSrcIndex, index);
    this.dragSrcIndex = null;
  }

  // Ermöglicht das Drag & Drop von Bildern in einen speziellen Bereich, um Dateien hochzuladen.
  onDragOverBox(event: DragEvent) {
    event.preventDefault();
  }

  // Wenn Dateien in den speziellen Bereich fallen gelassen werden, werden sie dem Bild-Upload-Service hinzugefügt.
  onDropBox(event: DragEvent) {
    event.preventDefault();
    if (event.dataTransfer?.files) {
      this.onFileSelected({ target: { files: event.dataTransfer.files } } as any);
    }
  }
}
