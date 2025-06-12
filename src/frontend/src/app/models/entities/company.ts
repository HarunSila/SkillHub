import { AddressT } from "../addressT";
import { OpeningTimeT } from "../openingTimeT";

export interface Company {
    id?: string;
    name?: string;
    contactEmail?: string;
    contactPhone?: string;
    registrationDate?: Date;
    address?: AddressT;
    openingTimes?: OpeningTimeT[];
}