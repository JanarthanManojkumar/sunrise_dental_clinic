package util;

import java.sql.SQLException;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Integration test: generate() counts existing rows for the date via the
 * real local MySQL instance, so this needs the DB from PROJECT_PLAN's schema
 * to be reachable. A far-future date is used so it never collides with real
 * seeded/registered appointments.
 */
public class AppointmentNumberGeneratorTest {

    @Test
    public void generatedNumberMatchesExpectedFormat() throws SQLException {
        LocalDate farFutureDate = LocalDate.now().plusYears(5);
        String appointmentNo = AppointmentNumberGenerator.generate(farFutureDate);
        assertTrue(appointmentNo.matches("APT-\\d{8}-\\d{3}"),
                "Expected APT-YYYYMMDD-### but got " + appointmentNo);
    }
}
