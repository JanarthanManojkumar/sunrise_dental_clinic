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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
    public void insertThenFindByAppointmentNoReturnsFullJoinedRecord() throws SQLException {
        LocalDate date = LocalDate.now().plusYears(5);
        Appointment appointment = buildAppointment("APT-TEST-" + System.nanoTime(), date, LocalTime.of(9, 0));

        int id = appointmentDAO.insert(appointment);
        insertedAppointmentIds.add(id);

        Appointment found = appointmentDAO.findByAppointmentNo(appointment.getAppointmentNo());
        assertEquals(fixturePatient.getName(), found.getPatient().getName());
        assertEquals(fixtureDentist.getName(), found.getDentist().getName());
        assertEquals(fixtureTreatment.getName(), found.getTreatment().getName());
        assertEquals(AppointmentStatus.SCHEDULED, found.getStatus());
    }

    @Test
    public void doubleBookingSameDentistSlotThrowsIntegrityViolation() throws SQLException {
        LocalDate date = LocalDate.now().plusYears(5);
        LocalTime time = LocalTime.of(11, 0);
        Appointment first = buildAppointment("APT-TEST-A-" + System.nanoTime(), date, time);
        int firstId = appointmentDAO.insert(first);
        insertedAppointmentIds.add(firstId);

        Appointment second = buildAppointment("APT-TEST-B-" + System.nanoTime(), date, time);

        assertThrows(SQLIntegrityConstraintViolationException.class, () -> appointmentDAO.insert(second));
    }

    @Test
    public void cancelSetsStatusToCancelled() throws SQLException {
        LocalDate date = LocalDate.now().plusYears(5);
        Appointment appointment = buildAppointment("APT-TEST-C-" + System.nanoTime(), date, LocalTime.of(14, 0));
        int id = appointmentDAO.insert(appointment);
        insertedAppointmentIds.add(id);

        appointmentDAO.cancel(id);

        Appointment found = appointmentDAO.findByAppointmentNo(appointment.getAppointmentNo());
        assertEquals(AppointmentStatus.CANCELLED, found.getStatus());
    }

    @Test
    public void findUpcomingIncludesFutureScheduledAppointment() throws SQLException {
        LocalDate date = LocalDate.now().plusYears(5);
        Appointment appointment = buildAppointment("APT-TEST-D-" + System.nanoTime(), date, LocalTime.of(15, 0));
        int id = appointmentDAO.insert(appointment);
        insertedAppointmentIds.add(id);

        boolean present = appointmentDAO.findUpcoming().stream().anyMatch(a -> a.getId() == id);

        assertTrue(present);
    }
}
