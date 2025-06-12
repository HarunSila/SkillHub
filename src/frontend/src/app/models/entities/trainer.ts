import { Availability } from "./availability";
import { TrainerStatusET } from "../trainerStatusET";
import { UserAccount } from "./userAccount";

export interface Trainer extends UserAccount {
    status: TrainerStatusET;
    availabilities: Availability[];
}