package model;

import java.time.LocalDate;

public record DailyAppointmentCount(LocalDate date, int count) {
}
