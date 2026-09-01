package controller;

import dao.AppointmentDAO;
import dao.PatientDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalTime;
import model.Appointment;
import model.Dentist;
import model.Treatment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AppointmentControllerTest {

    private AppointmentDAO appointmentDAO;
    private PatientDAO patientDAO;
    private AppointmentController appointmentController;

    private final Dentist dentist = new Dentist(1, "Dr. Nimal Perera", "General Dentistry");
    private final Treatment treatment = new Treatment(1, "Filling", new BigDecimal("4500.00"));
    private final LocalDate futureDate = LocalDate.now().plusYears(5);
    private final LocalTime time = LocalTime.of(10, 0);

    @BeforeEach
    public void setUp() {
        appointmentDAO = mock(AppointmentDAO.class);
        patientDAO = mock(PatientDAO.class);
        appointmentController = new AppointmentController(appointmentDAO, patientDAO);
    }

    @Test
    public void emptyPatientNameFailsValidation() {
        String result = appointmentController.validateAppointment("", "0771234567", dentist, treatment,
                futureDate, time);
        assertEquals("Patient name is required", result);
    }

    @Test
    public void emptyContactNumberFailsValidation() {
        String result = appointmentController.validateAppointment("John Silva", " ", dentist, treatment,
                futureDate, time);
        assertEquals("Contact number is required", result);
    }

    @Test
    public void missingDentistFailsValidation() {
        String result = appointmentController.validateAppointment("John Silva", "0771234567", null, treatment,
                futureDate, time);
        assertEquals("Dentist must be selected", result);
    }

    @Test
    public void missingTreatmentFailsValidation() {
        String result = appointmentController.validateAppointment("John Silva", "0771234567", dentist, null,
                futureDate, time);
        assertEquals("Treatment must be selected", result);
    }

    @Test
    public void pastDateFailsValidation() {
        String result = appointmentController.validateAppointment("John Silva", "0771234567", dentist, treatment,
                LocalDate.now().minusDays(1), time);
        assertEquals("Appointment date cannot be in the past", result);
    }

    @Test
    public void missingTimeFailsValidation() {
        String result = appointmentController.validateAppointment("John Silva", "0771234567", dentist, treatment,
                futureDate, null);
        assertEquals("Appointment time is required", result);
    }

    @Test
    public void validAppointmentPassesValidation() {
        String result = appointmentController.validateAppointment("John Silva", "0771234567", dentist, treatment,
                futureDate, time);
        assertEquals("VALID", result);
    }

    @Test
    public void registerAppointmentRejectsInvalidInputWithoutTouchingDao() throws SQLException {
        ControllerResult<Appointment> result = appointmentController.registerAppointment("", "Addr",
                "0771234567", "john@example.com", dentist, treatment, futureDate, time);

        assertFalse(result.isSuccess());
        assertEquals("Patient name is required", result.getMessage());
    }

    @Test
    public void registerAppointmentReturnsFriendlyMessageOnDoubleBooking() throws SQLException {
        doThrow(new SQLIntegrityConstraintViolationException(
                "Duplicate entry '1-2026-08-31-10:00:00' for key 'appointments.uq_dentist_slot'"))
                .when(appointmentDAO).insert(any(Appointment.class));

        ControllerResult<Appointment> result = appointmentController.registerAppointment("John Silva", "Addr",
                "0771234567", "john@example.com", dentist, treatment, futureDate, time);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("already has an appointment"));
    }

    @Test
    public void searchAppointmentReturnsFailureWhenNotFound() throws SQLException {
        org.mockito.Mockito.when(appointmentDAO.findByAppointmentNo("APT-NOTFOUND")).thenReturn(null);

        ControllerResult<Appointment> result = appointmentController.searchAppointment("APT-NOTFOUND");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("No appointment found"));
    }
}
