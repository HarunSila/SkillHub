import { Component, inject, Input, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { Cours } from '../../../models/entities/cours';
import { CoursService } from '../../../services/api/coursApi.service';
import { Availability } from '../../../models/entities/availability';
import { CommonModule } from '@angular/common';

/*
  * Carousel-Komponente für Kursbilder
  * Zeigt Bilder eines Kurses an und ermöglicht das Durchblättern der Bilder.
*/

@Component({
  selector: 'app-carousel',
  standalone: true,
  imports: [MatTabsModule, MatButtonModule, MatIconModule, MatCardModule, CommonModule],
  templateUrl: './carousel.component.html',
  styleUrl: './carousel.component.scss'
})
export class CarouselComponent implements OnInit {

  @Input() cours!: Cours;

  private readonly coursService = inject(CoursService);

  pictureUrls = ['assets/logo.jpg']; // Standardbild, falls keine Bilder vorhanden sind
  currentImageIndex: number = 0;

  ngOnInit(): void {
    this.pictureUrls = this.getPictures();
  }

  nextImage() {
    if (this.currentImageIndex < this.pictureUrls.length - 1)
      this.currentImageIndex++;
    else this.currentImageIndex = 0;
  }

  previousImage() {
    if (this.currentImageIndex > 0)
      this.currentImageIndex--;
    else this.currentImageIndex = this.pictureUrls.length - 1;
  }

  getCurrentImage() {
    return this.pictureUrls[this.currentImageIndex]
  }

  getTrainerName(availability: Availability) {
    if (availability && availability.trainer)
      return `${availability.trainer.name ?? ''} ${availability.trainer.surname ?? ''}`.trim();
    else return '';
  }

  getPictures() {
    if (!this.cours || !this.cours.pictureUrls || this.cours.pictureUrls.length === 0)
      return ['assets/logo.jpg']; // Standardbild, falls keine Bilder vorhanden sind
    else {
      let pictureUrls: string[] = [];
      this.cours.pictureUrls.forEach(url => pictureUrls.push(this.coursService.providePictureUrl(url)));
      return pictureUrls;
    }
  }
}