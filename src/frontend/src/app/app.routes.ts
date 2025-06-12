import { Routes } from '@angular/router';
import { ConfigurationComponent } from './components/views/configuration/configuration.component';
import { CoursesComponent } from './components/views/courses/courses.component';
import { UsersComponent } from './components/views/users/users.component';
import { LocationManagementComponent } from './components/views/location-management/location-management.component';
import { MyCoursesComponent } from './components/views/my-courses/my-courses.component';
import { CoursEditorComponent } from './components/views/cours-editor/cours-editor.component';
import { AuthGuard } from './guard & interceptor/auth.guard';
import { CoursParticipantsComponent } from './components/views/cours-participants/cours-participants.component';

export const routes: Routes = [
    {path: 'configuration', component: ConfigurationComponent, canActivate: [AuthGuard]},
    {path: 'courses', component: CoursesComponent, canActivate: [AuthGuard]},
    {path: 'users', component: UsersComponent, canActivate: [AuthGuard], data: {role: 'admin'}},
    {path: 'location-management', component: LocationManagementComponent, canActivate: [AuthGuard], data: {role: 'admin'}},
    {path: 'my-courses', component: MyCoursesComponent, canActivate: [AuthGuard]},
    {path: 'cours-editor', component: CoursEditorComponent, canActivate: [AuthGuard], data: {role: 'trainer'}},
    {path: 'cours-editor/:id', component: CoursEditorComponent, canActivate: [AuthGuard], data: {role: 'trainer'}},
    {path: 'cours-participants/:id', component: CoursParticipantsComponent, canActivate: [AuthGuard], data: {role: 'trainer', alternative: 'admin'}},
    {path: '', redirectTo: 'courses', pathMatch: 'full'},
];
