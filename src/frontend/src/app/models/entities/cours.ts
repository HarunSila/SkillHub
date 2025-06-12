
import { Availability } from './availability';
import { CoursRegistration } from './coursRegistration';

export interface Cours {
    id?: string;
    title: string;
    description: string;
    startDate: Date;
    endDate: Date;
    maxParticipants: number;
    pictureUrls?: string[];
    availabilities: Availability[];
    registrations?: CoursRegistration[];
}