import { CoursRegistration } from "./coursRegistration";
import { UserAccount } from "./userAccount";


export interface Participant extends UserAccount {
    coursRegistrations?: CoursRegistration[];
}