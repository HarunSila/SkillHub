import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { EquipmentModalComponent } from './equipment-modal/equipment-modal.component';
import { MatIconModule } from '@angular/material/icon';
import { Location } from '../../../models/entities/location';
import { LocationManagementService } from '../../../services/api/location-managementApi.service';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { AddLocationComponent } from "./add-location/add-location.component";

/*
 * LocationManagementComponent ermöglicht die Verwaltung von Standorten.
  * Es zeigt eine Liste von Standorten an, ermöglicht das Hinzufügen, Bearbeiten und Löschen von Standorten
  * und verwendet den LocationManagementService, um Standorte zu verwalten.
*/

@Component({
  selector: 'app-location-management',
  standalone: true,
  imports: [
    MatCardModule, MatButtonModule, CommonModule, MatSnackBarModule,
    MatDialogModule, MatIconModule, MatTableModule, MatPaginatorModule, MatSortModule,
    AddLocationComponent
],
  templateUrl: './location-management.component.html',
  styleUrl: './location-management.component.scss'
})
export class LocationManagementComponent implements OnInit {

  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly locationManagementService = inject(LocationManagementService);

  displayedColumns: string[] = ['name', 'capacity', 'equipmentList', 'status', 'actions'];  

  locations: Location[] = [];

  locationToEdit: Location | null = null;

  dataSource = new MatTableDataSource<any>(this.locations);
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;


  // Initialisiert die Komponente und lädt die Standorte
  ngOnInit(): void {
      this.locationManagementService.getLocations().subscribe({
        next: (locations) => {
          if(locations) this.locations = locations;
          this.initDataSource();
        },
        error: (error) => {
          console.error('Error fetching locations:', error);
          this.snackBar.open('Error fetching locations', 'Close');
        }
      });
  }
  
  // Aktualisiert die Datenquelle, wenn sich die Standorte ändern
  editLocation(location: Location) {
    this.locationToEdit = null;
    this.cdr.detectChanges();
    this.locationToEdit = location;
    this.cdr.detectChanges();
  }

  // Löscht einen Standort
  deleteLocation(location: Location) {
    const dialogRef = this.dialog.open(EquipmentModalComponent, {
      width: '400px',
      data: 'delete-location'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'delete') {
        this.locationManagementService.deleteLocation(location.id!).subscribe({
          next: () => {
            this.snackBar.open('Location deleted successfully', 'Close', { duration: 2000 });
            this.locations = this.locations.filter(loc => loc.id !== location.id);
            this.initDataSource();
            this.cdr.detectChanges();
          },
          error: (error) => {
            console.error('Error deleting location: ', error);
            this.snackBar.open('Error deleting location.', 'Close');
          }
        });
      }
    });
  }

  // Filtert die Standorte basierend auf dem eingegebenen Wert
  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  // Initialisiert die Datenquelle für die Tabelle
  initDataSource() {
    this.dataSource = new MatTableDataSource<any>(this.locations);
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
    this.cdr.detectChanges();
  }

  // Gibt den Status eines Standorts zurück
  getLocationStatus(location: Location): string {
    return location.status.active === true ? 'Active' : 'Inactive';
  }

  // Nach dem Hinzufügen eines Standorts wird die Tabelle aktualisiert
  onLocationAdded(locations: Location[]) {
    this.locations = locations
    this.initDataSource();
  }
}
