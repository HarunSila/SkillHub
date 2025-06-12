import { Location } from "./location";

export interface Equipment {
    id?: string;
    name: string;
    description: string;
    amount: number;
    location?: Location;
}