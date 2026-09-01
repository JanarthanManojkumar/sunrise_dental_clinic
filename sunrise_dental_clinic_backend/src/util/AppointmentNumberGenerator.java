package util;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Appointment numbers are app-generated (APT-YYYYMMDD-###), never typed by
 * staff, so duplicate/clashing appointment numbers can't happen by user error.
 */
public final class AppointmentNumberGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private AppointmentNumberGenerator() {
    }

    public static String generate(LocalDate date) throws SQLException {
        String datePart = date.format(DATE_FORMAT);
        int countToday = countAppointmentsOn(date);
        int sequence = countToday + 1;
        return String.format("APT-%s-%03d", datePart, sequence);
    }

    private static int countAppointmentsOn(LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setObject(1, date);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
