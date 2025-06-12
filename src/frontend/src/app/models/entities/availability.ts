import { Cours } from "./cours";
import { DayET } from "../dayET";
import { Trainer } from "./trainer";
import { Location } from "./location";

export interface Availability {
    id?: string;
    trainer?: Trainer;
    cours?: Cours;
    location?: Location;
    weekday: DayET;
    startTime: string;
    endTime: string;
}