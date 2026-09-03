package dao;

import db.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import model.Appointment;
import model.AppointmentStatus;
import model.Dentist;
import model.Patient;
import model.Treatment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration test against the real local MySQL database. Each test creates
 * its own throwaway dentist/treatment/patient fixture (never touching the
 * seeded reference rows) and every inserted row is deleted in @AfterEach.
 */
public class AppointmentDAOIntegrationTest {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DentistDAO dentistDAO = new DentistDAO();
    private final TreatmentDAO treatmentDAO = new TreatmentDAO();

    private Dentist fixtureDentist;
    private Treatment fixtureTreatment;
    private Patient fixturePatient;
    private Dentist extraDentist;
    private Treatment extraTreatment;
    private final List<Integer> insertedAppointmentIds = new ArrayList<>();

    @BeforeEach
    public void setUp() throws SQLException {
        fixtureDentist = new Dentist();
        fixtureDentist.setName("Dr. Fixture " + System.nanoTime());
        fixtureDentist.setSpecialization("General Dentistry");
        dentistDAO.insert(fixtureDentist);

        fixtureTreatment = new Treatment();
        fixtureTreatment.setName("Fixture Treatment " + System.nanoTime());
        fixtureTreatment.setFee(new BigDecimal("1000.00"));
        treatmentDAO.insert(fixtureTreatment);

        fixturePatient = new Patient();
        fixturePatient.setName("Fixture Patient");
        fixturePatient.setAddress("Test Address");
        fixturePatient.setContactNumber("0770000001");
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
        if (extraDentist != null) {
            try (PreparedStatement pst = con.prepareStatement("DELETE FROM dentists WHERE id = ?")) {
                pst.setInt(1, extraDentist.getId());
                pst.executeUpdate();
            }
        }
        if (extraTreatment != null) {
            try (PreparedStatement pst = con.prepareStatement("DELETE FROM treatments WHERE id = ?")) {
                pst.setInt(1, extraTreatment.getId());
                pst.executeUpdate();
            }
        }
    }

    /** Short unique numeric suffix that safely fits the appointment_no VARCHAR(30) column. */
    private static long shortId() {
        return System.nanoTime() % 1_000_000L;
    }

    private Appointment buildAppointment(String appointmentNo, LocalDate date, LocalTime time) {
        return new Appointment.Builder()
                .appointmentNo(appointmentNo)
                .patient(fixturePatient)
                .dentist(fixtureDentist)
                .treatment(fixtureTreatment)
                .appointmentDate(date)
                .appointmentTime(time)
                .build();
    }

    @Test
    @DisplayName("Booking a new appointment can be found again by its appointment number with full patient, dentist and treatment details")
    public void insertThenFindByAppointmentNoReturnsFullJoinedRecord() throws SQLException {
        LocalDate date = LocalDate.now().plusYears(5);
        Appointment appointment = buildAppointment("APT-TEST-" + shortId(), date, LocalTime.of(9, 0));

        int id = appointmentDAO.insert(appointment);
        insertedAppointmentIds.add(id);

        Appointment found = appointmentDAO.findByAppointmentNo(appointment.getAppointmentNo());
        assertEquals(fixturePatient.getName(), found.getPatient().getName());
        assertEquals(fixtureDentist.getName(), found.getDentist().getName());
        assertEquals(fixtureTreatment.getName(), found.getTreatment().getName());
        assertEquals(AppointmentStatus.SCHEDULED, found.getStatus());
    }

    @Test
    @DisplayName("Booking two appointments for the same dentist at the same date and time is rejected")
    public void doubleBookingSameDentistSlotThrowsIntegrityViolation() throws SQLException {
        LocalDate date = LocalDate.now().plusYears(5);
        LocalTime time = LocalTime.of(11, 0);
        Appointment first = buildAppointment("APT-TEST-A-" + shortId(), date, time);
        int firstId = appointmentDAO.insert(first);
        insertedAppointmentIds.add(firstId);

        Appointment second = buildAppointment("APT-TEST-B-" + shortId(), date, time);

        assertThrows(SQLIntegrityConstraintViolationException.class, () -> appointmentDAO.insert(second));
    }

    @Test
    @DisplayName("Rescheduling an appointment to a new date and time updates the stored record")
    public void rescheduleUpdatesAppointmentDateAndTime() throws SQLException {
        LocalDate originalDate = LocalDate.now().plusYears(5);
        Appointment appointment = buildAppointment("APT-TEST-RE-" + shortId(), originalDate, LocalTime.of(10, 0));
        int id = appointmentDAO.insert(appointment);
        insertedAppointmentIds.add(id);

        LocalDate newDate = originalDate.plusDays(3);
        LocalTime newTime = LocalTime.of(16, 30);
        Appointment reschedule = new Appointment.Builder()
                .id(id)
                .dentist(fixtureDentist)
                .treatment(fixtureTreatment)
                .appointmentDate(newDate)
                .appointmentTime(newTime)
                .build();
        appointmentDAO.update(reschedule);

        Appointment found = appointmentDAO.findByAppointmentNo(appointment.getAppointmentNo());
        assertEquals(newDate, found.getAppointmentDate());
        assertEquals(newTime, found.getAppointmentTime());
    }

    @Test
    @DisplayName("Editing an appointment to use a different dentist and treatment updates both on the record")
    public void editAppointmentReassignsDentistAndTreatment() throws SQLException {
        LocalDate date = LocalDate.now().plusYears(5);
        Appointment appointment = buildAppointment("APT-TEST-EDIT-" + shortId(), date, LocalTime.of(10, 30));
        int id = appointmentDAO.insert(appointment);
        insertedAppointmentIds.add(id);

        extraDentist = new Dentist();
        extraDentist.setName("Dr. Extra Fixture " + System.nanoTime());
        extraDentist.setSpecialization("Orthodontics");
        dentistDAO.insert(extraDentist);

        extraTreatment = new Treatment();
        extraTreatment.setName("Extra Fixture Treatment " + System.nanoTime());
        extraTreatment.setFee(new BigDecimal("2000.00"));
        treatmentDAO.insert(extraTreatment);

        Appointment edited = new Appointment.Builder()
                .id(id)
                .dentist(extraDentist)
                .treatment(extraTreatment)
                .appointmentDate(date)
                .appointmentTime(LocalTime.of(10, 30))
                .build();
        appointmentDAO.update(edited);

        Appointment found = appointmentDAO.findByAppointmentNo(appointment.getAppointmentNo());
        assertEquals(extraDentist.getName(), found.getDentist().getName());
        assertEquals(extraTreatment.getName(), found.getTreatment().getName());
    }

    @Test
    @DisplayName("Cancelling an appointment marks its status as cancelled")
    public void cancelSetsStatusToCancelled() throws SQLException {
        LocalDate date = LocalDate.now().plusYears(5);
        Appointment appointment = buildAppointment("APT-TEST-C-" + shortId(), date, LocalTime.of(14, 0));
        int id = appointmentDAO.insert(appointment);
        insertedAppointmentIds.add(id);

        appointmentDAO.cancel(id);

        Appointment found = appointmentDAO.findByAppointmentNo(appointment.getAppointmentNo());
        assertEquals(AppointmentStatus.CANCELLED, found.getStatus());
    }

    @Test
    @DisplayName("A future scheduled appointment appears in the upcoming appointments list")
    public void findUpcomingIncludesFutureScheduledAppointment() throws SQLException {
        LocalDate date = LocalDate.now().plusYears(5);
        Appointment appointment = buildAppointment("APT-TEST-D-" + shortId(), date, LocalTime.of(15, 0));
        int id = appointmentDAO.insert(appointment);
        insertedAppointmentIds.add(id);

        boolean present = appointmentDAO.findUpcoming().stream().anyMatch(a -> a.getId() == id);

        assertTrue(present);
    }

    @Test
    @DisplayName("A cancelled appointment is excluded from the upcoming appointments list")
    public void findUpcomingExcludesCancelledAppointment() throws SQLException {
        LocalDate date = LocalDate.now().plusYears(5);
        Appointment appointment = buildAppointment("APT-TEST-UPC-" + shortId(), date, LocalTime.of(15, 30));
        int id = appointmentDAO.insert(appointment);
        insertedAppointmentIds.add(id);

        appointmentDAO.cancel(id);

        boolean present = appointmentDAO.findUpcoming().stream().anyMatch(a -> a.getId() == id);

        assertFalse(present);
    }

    @Test
    @DisplayName("A past-dated appointment does not appear in the upcoming appointments list even though it is still marked scheduled")
    public void findUpcomingExcludesPastAppointment() throws SQLException {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Appointment appointment = buildAppointment("APT-TEST-UPP-" + shortId(), pastDate, LocalTime.of(9, 0));
        int id = appointmentDAO.insert(appointment);
        insertedAppointmentIds.add(id);

        boolean present = appointmentDAO.findUpcoming().stream().anyMatch(a -> a.getId() == id);

        assertFalse(present);
    }

    @Test
    @DisplayName("Filtering appointments by an exact date returns only appointments booked on that date")
    public void findFilteredByExactDateReturnsOnlyAppointmentsOnThatDate() throws SQLException {
        LocalDate targetDate = LocalDate.now().plusYears(5);
        LocalDate otherDate = targetDate.plusDays(1);

        Appointment onTarget = buildAppointment("APT-TEST-FA-" + shortId(), targetDate, LocalTime.of(8, 0));
        int onTargetId = appointmentDAO.insert(onTarget);
        insertedAppointmentIds.add(onTargetId);

        Appointment onOther = buildAppointment("APT-TEST-FB-" + shortId(), otherDate, LocalTime.of(8, 0));
        int onOtherId = appointmentDAO.insert(onOther);
        insertedAppointmentIds.add(onOtherId);

        List<Appointment> results = appointmentDAO.findFiltered(targetDate, null, null, null, null);

        assertTrue(results.stream().anyMatch(a -> a.getId() == onTargetId));
        assertFalse(results.stream().anyMatch(a -> a.getId() == onOtherId));
    }

    @Test
    @DisplayName("Filtering appointments by a date range together with a dentist returns only that dentist's appointments in range")
    public void findFilteredByDateRangeAndDentistReturnsOnlyMatchingAppointments() throws SQLException {
        LocalDate inRangeDate = LocalDate.now().plusYears(5);
        LocalDate outOfRangeDate = inRangeDate.plusMonths(2);

        Appointment inRangeForFixtureDentist = buildAppointment("APT-TEST-FC-" + shortId(), inRangeDate, LocalTime.of(9, 30));
        int inRangeId = appointmentDAO.insert(inRangeForFixtureDentist);
        insertedAppointmentIds.add(inRangeId);

        Appointment outOfRangeForFixtureDentist = buildAppointment("APT-TEST-FD-" + shortId(), outOfRangeDate, LocalTime.of(9, 30));
        int outOfRangeId = appointmentDAO.insert(outOfRangeForFixtureDentist);
        insertedAppointmentIds.add(outOfRangeId);

        extraDentist = new Dentist();
        extraDentist.setName("Dr. Extra Fixture " + System.nanoTime());
        extraDentist.setSpecialization("Orthodontics");
        dentistDAO.insert(extraDentist);

        Appointment inRangeForExtraDentist = new Appointment.Builder()
                .appointmentNo("APT-TEST-FE-" + shortId())
                .patient(fixturePatient)
                .dentist(extraDentist)
                .treatment(fixtureTreatment)
                .appointmentDate(inRangeDate)
                .appointmentTime(LocalTime.of(9, 30))
                .build();
        int extraDentistAppointmentId = appointmentDAO.insert(inRangeForExtraDentist);
        insertedAppointmentIds.add(extraDentistAppointmentId);

        List<Appointment> results = appointmentDAO.findFiltered(null, inRangeDate, inRangeDate, fixtureDentist.getId(), null);

        assertTrue(results.stream().anyMatch(a -> a.getId() == inRangeId));
        assertFalse(results.stream().anyMatch(a -> a.getId() == outOfRangeId));
        assertFalse(results.stream().anyMatch(a -> a.getId() == extraDentistAppointmentId));
    }

    @Test
    @DisplayName("Filtering appointments by patient name or contact number returns matching bookings")
    public void findFilteredByPatientQueryMatchesNameOrContactNumber() throws SQLException {
        LocalDate date = LocalDate.now().plusYears(5);
        Appointment appointment = buildAppointment("APT-TEST-FF-" + shortId(), date, LocalTime.of(13, 0));
        int id = appointmentDAO.insert(appointment);
        insertedAppointmentIds.add(id);

        List<Appointment> byName = appointmentDAO.findFiltered(null, null, null, null, "Fixture Patient");
        assertTrue(byName.stream().anyMatch(a -> a.getId() == id));

        List<Appointment> byContact = appointmentDAO.findFiltered(null, null, null, null, fixturePatient.getContactNumber());
        assertTrue(byContact.stream().anyMatch(a -> a.getId() == id));
    }

    @Test
    @DisplayName("Looking up appointments by a patient's contact number returns their booking with dentist and treatment details")
    public void findByContactNumberReturnsAppointmentsForThatPatient() throws SQLException {
        LocalDate date = LocalDate.now().plusYears(5);
        Appointment appointment = buildAppointment("APT-TEST-CN-" + shortId(), date, LocalTime.of(12, 0));
        int id = appointmentDAO.insert(appointment);
        insertedAppointmentIds.add(id);

        List<Appointment> results = appointmentDAO.findByContactNumber(fixturePatient.getContactNumber());

        assertTrue(results.stream().anyMatch(a -> a.getId() == id
                && a.getDentist().getName().equals(fixtureDentist.getName())
                && a.getTreatment().getName().equals(fixtureTreatment.getName())));
    }

    @Test
    @DisplayName("The appointments-per-day report includes the date of a newly booked appointment")
    public void countPerDayReflectsNewlyBookedAppointment() throws SQLException {
        LocalDate date = LocalDate.now().plusYears(5);
        Appointment appointment = buildAppointment("APT-TEST-CT-" + shortId(), date, LocalTime.of(10, 0));
        int id = appointmentDAO.insert(appointment);
        insertedAppointmentIds.add(id);

        boolean present = appointmentDAO.countPerDay().stream()
                .anyMatch(c -> c.date().equals(date) && c.count() >= 1);

        assertTrue(present);
    }
}
