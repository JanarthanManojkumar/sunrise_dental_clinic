package util;

import dao.AppointmentDAO;
import dao.DentistDAO;
import dao.PatientDAO;
import dao.TreatmentDAO;
import db.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.Appointment;
import model.Dentist;
import model.Patient;
import model.Treatment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration test: generate() counts existing rows for the date via the
 * real local MySQL instance, so this needs the DB from PROJECT_PLAN's schema
 * to be reachable. Far-future dates are used so tests never collide with
 * real seeded/registered appointments, and each test picks its own
 * dedicated far-future date so the count-based assertions below can't be
 * thrown off by other tests/fixtures running against the same date.
 *
 * NOTE: the SQLException-during-count path (countAppointmentsOn's catch
 * branch) is intentionally NOT covered here. AppointmentNumberGenerator is
 * a final, all-static class with no constructor injection, and it reaches
 * the DB through the hand-rolled DBConnection singleton (private
 * constructor, no seam to swap in a failing Connection). Faking that
 * failure would require Mockito's static mocking (mockStatic), which needs
 * the inline mock maker; this project's lib/mockito-core jar has no
 * mockito-extensions/org.mockito.plugins.MockMaker registration for it and
 * no other test in this suite uses static mocking. Forcing it in here would
 * mean adding new mocking infrastructure for one fragile test. Exercising
 * that branch for real would require refactoring AppointmentNumberGenerator
 * to accept an injectable connection/count-supplier - out of scope for this
 * test-only change.
 */
public class AppointmentNumberGeneratorTest {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DentistDAO dentistDAO = new DentistDAO();
    private final TreatmentDAO treatmentDAO = new TreatmentDAO();

    private Dentist fixtureDentist;
    private Treatment fixtureTreatment;
    private Patient fixturePatient;
    private final List<Integer> insertedAppointmentIds = new ArrayList<>();

    @BeforeEach
    public void setUp() throws SQLException {
        fixtureDentist = new Dentist();
        fixtureDentist.setName("Dr. SeqFixture " + System.nanoTime());
        fixtureDentist.setSpecialization("General Dentistry");
        dentistDAO.insert(fixtureDentist);

        fixtureTreatment = new Treatment();
        fixtureTreatment.setName("Sequence Fixture Treatment " + System.nanoTime());
        fixtureTreatment.setFee(new BigDecimal("1000.00"));
        treatmentDAO.insert(fixtureTreatment);

        fixturePatient = new Patient();
        fixturePatient.setName("Sequence Fixture Patient");
        fixturePatient.setAddress("Test Address");
        fixturePatient.setContactNumber("0770000002");
        patientDAO.insert(fixturePatient);
    }

    @AfterEach
    public void cleanUp() throws SQLException {
        Connection con = DBConnection.getInstance().getConnection();
        for (int id : insertedAppointmentIds) {
            try (PreparedStatement pst = con.prepareStatement("DELETE FROM appointments WHERE id = ?")) {
                pst.setInt(1, id);
                pst.executeUpdate();
            }
        }
        try (PreparedStatement pst = con.prepareStatement("DELETE FROM patients WHERE id = ?")) {
            pst.setInt(1, fixturePatient.getId());
            pst.executeUpdate();
        }
        try (PreparedStatement pst = con.prepareStatement("DELETE FROM dentists WHERE id = ?")) {
            pst.setInt(1, fixtureDentist.getId());
            pst.executeUpdate();
        }
        try (PreparedStatement pst = con.prepareStatement("DELETE FROM treatments WHERE id = ?")) {
            pst.setInt(1, fixtureTreatment.getId());
            pst.executeUpdate();
        }
    }

    /** Inserts a throwaway appointment fixture row so generate()'s COUNT(*) sees it. */
    private void insertThrowawayAppointment(LocalDate date, LocalTime time) throws SQLException {
        Appointment appointment = new Appointment.Builder()
                .appointmentNo("APT-SEQ-" + (System.nanoTime() % 1_000_000L))
                .patient(fixturePatient)
                .dentist(fixtureDentist)
                .treatment(fixtureTreatment)
                .appointmentDate(date)
                .appointmentTime(time)
                .build();
        int id = appointmentDAO.insert(appointment);
        insertedAppointmentIds.add(id);
    }

    private String expectedAppointmentNo(LocalDate date, int sequence) {
        return String.format("APT-%s-%03d", date.format(DateTimeFormatter.ofPattern("yyyyMMdd")), sequence);
    }

    @Test
    @DisplayName("Generated appointment number matches the APT-YYYYMMDD-### format")
    public void generatedNumberMatchesExpectedFormat() throws SQLException {
        LocalDate farFutureDate = LocalDate.now().plusYears(5);
        String appointmentNo = AppointmentNumberGenerator.generate(farFutureDate);
        assertTrue(appointmentNo.matches("APT-\\d{8}-\\d{3}"),
                "Expected APT-YYYYMMDD-### but got " + appointmentNo);
    }

    @Test
    @DisplayName("Sequence number increments to N+1 when N appointments already exist on the date")
    public void sequenceIncrementsWhenAppointmentsAlreadyExistOnDate() throws SQLException {
        LocalDate date = LocalDate.now().plusYears(6);
        int existingCount = 3;
        for (int i = 0; i < existingCount; i++) {
            insertThrowawayAppointment(date, LocalTime.of(9, 0).plusMinutes(30L * i));
        }

        String appointmentNo = AppointmentNumberGenerator.generate(date);

        assertEquals(expectedAppointmentNo(date, existingCount + 1), appointmentNo);
    }

    @Test
    @DisplayName("A date with no existing appointments always restarts its sequence at 001, regardless of another date's count")
    public void differentDatesRestartSequenceIndependently() throws SQLException {
        LocalDate dateWithAppointments = LocalDate.now().plusYears(7);
        LocalDate dateWithNone = LocalDate.now().plusYears(8);

        insertThrowawayAppointment(dateWithAppointments, LocalTime.of(9, 0));
        insertThrowawayAppointment(dateWithAppointments, LocalTime.of(9, 30));

        String appointmentNoForBusyDate = AppointmentNumberGenerator.generate(dateWithAppointments);
        String appointmentNoForEmptyDate = AppointmentNumberGenerator.generate(dateWithNone);

        assertEquals(expectedAppointmentNo(dateWithAppointments, 3), appointmentNoForBusyDate);
        assertEquals(expectedAppointmentNo(dateWithNone, 1), appointmentNoForEmptyDate);
    }
}
