import { Cours } from "./cours";
import { Participant } from "./participant";
import { RegistrationStatusET } from "../registrationStatusET";

export interface CoursRegistration {
    id?: string;
    registrationDate: Date;
    status: RegistrationStatusET;
    participant?: Participant;
    cours: Cours;
}