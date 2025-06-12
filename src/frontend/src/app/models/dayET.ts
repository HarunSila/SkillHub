export enum DayET {
    MONDAY = 'Monday',
    TUESDAY = 'Tuesday',
    WEDNESDAY = 'Wednesday',
    THURSDAY = 'Thursday',
    FRIDAY = 'Friday',
    SATURDAY = 'Saturday',
    SUNDAY = 'Sunday'
}

export const DayOrder: DayET[] = [
    DayET.MONDAY,
    DayET.TUESDAY,
    DayET.WEDNESDAY,
    DayET.THURSDAY,
    DayET.FRIDAY,
    DayET.SATURDAY,
    DayET.SUNDAY
];

export function getKeyByDayET(day: DayET): string {
    return Object.keys(DayET).find(key => DayET[key as keyof typeof DayET] === day) || '';
}