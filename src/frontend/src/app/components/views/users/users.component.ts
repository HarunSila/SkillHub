import { Component, inject, OnInit } from '@angular/core';
import { Participant } from '../../../models/entities/participant';
import { Trainer } from '../../../models/entities/trainer';
import { UserAccount } from '../../../models/entities/userAccount';
import { ProfileApiService } from '../../../services/api/profileApi.service';
import { TableComponent } from './table/table.component';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [TableComponent, MatCardModule],
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss'
})
export class UsersComponent implements OnInit {
  private readonly profileApiService = inject(ProfileApiService);

  columns_unassignedRoles = ["Keycloak Id", "Username", "Name", "Email"];
  columns_trainer = ["Keycloak Id", "Username", "Name", "Email", "Status", "Details"];
  columns_participants = ["Keycloak Id", "Username", "Name", "Email", "Details"];

  uers_unassignedRoles: UserAccount[] = [];
  users_trainer: Trainer[] = [];
  users_participants: Participant[] = [];

  // Initialisiert die Komponente und lädt die Benutzer
  // mit unzugewiesenen Rollen, Trainern und Teilnehmern
  // und teilt sie in entsprechende Arrays auf.
  ngOnInit() {
    this.profileApiService.getAllUser().subscribe({
      next: (data) => {
        const unassigned: UserAccount[] = [];
        const trainers: Trainer[] = [];
        const participants: Participant[] = [];

        data.forEach((user: UserAccount | Trainer | Participant) => {
          if (user.role === undefined || user.role === null ) {
            unassigned.push(user as UserAccount);
          } else if (user.role === 'trainer') {
            trainers.push(user as Trainer);
          } else if (user.role === 'participant') {
            participants.push(user as Participant);
          }
        });

        this.uers_unassignedRoles = unassigned;
        this.users_trainer = trainers;
        this.users_participants = participants;
      },
      error: (error) => {
        console.error('Error fetching users:', error);
      }
    });
  }
}
