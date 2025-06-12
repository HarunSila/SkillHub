import { Equipment } from "./equipment";
import { LocationStatusT } from "../locationStatusT";
import { Availability } from "./availability";

export interface Location {
    id?: string;
    name: string;
    capacity: number;
    status: LocationStatusT;
    equipmentList: Equipment[];
    availabilityList?: Availability[];
}