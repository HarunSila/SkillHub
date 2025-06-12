package com.skillhub.backend.models;

public enum DayET {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    private final String day;

    DayET(String day) {
        this.day = day;
    }

    public String getDay() {
        return day;
    }
}
