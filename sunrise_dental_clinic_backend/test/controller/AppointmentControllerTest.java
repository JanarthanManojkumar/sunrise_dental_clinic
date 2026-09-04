package controller;

import dao.AppointmentDAO;
import dao.PatientDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import model.Appointment;
import model.AppointmentStatus;
import model.Dentist;
import model.Patient;
import model.Treatment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    @DisplayName("Booking fails when patient name is empty")
    public void emptyPatientNameFailsValidation() {
        String result = appointmentController.validateAppointment("", "0771234567", dentist, treatment,
                futureDate, time);
        assertEquals("Patient name is required", result);
    }

    @Test
    @DisplayName("Booking fails when contact number is empty")
    public void emptyContactNumberFailsValidation() {
        String result = appointmentController.validateAppointment("John Silva", " ", dentist, treatment,
                futureDate, time);
        assertEquals("Contact number is required", result);
    }

    @Test
    @DisplayName("Booking fails when no dentist is selected")
    public void missingDentistFailsValidation() {
        String result = appointmentController.validateAppointment("John Silva", "0771234567", null, treatment,
                futureDate, time);
        assertEquals("Dentist must be selected", result);
    }

    @Test
    @DisplayName("Booking fails when no treatment is selected")
    public void missingTreatmentFailsValidation() {
        String result = appointmentController.validateAppointment("John Silva", "0771234567", dentist, null,
                futureDate, time);
        assertEquals("Treatment must be selected", result);
    }

    @Test
    @DisplayName("Booking fails when the appointment date is in the past")
    public void pastDateFailsValidation() {
        String result = appointmentController.validateAppointment("John Silva", "0771234567", dentist, treatment,
                LocalDate.now().minusDays(1), time);
        assertEquals("Appointment date cannot be in the past", result);
    }

    @Test
    @DisplayName("Booking validation accepts an appointment date of exactly today")
    public void appointmentDateEqualToTodayPassesValidation() {
        String result = appointmentController.validateAppointment("John Silva", "0771234567", dentist, treatment,
                LocalDate.now(), time);
        assertEquals("VALID", result);
    }

    @Test
    @DisplayName("Booking fails when the appointment time is missing")
    public void missingTimeFailsValidation() {
        String result = appointmentController.validateAppointment("John Silva", "0771234567", dentist, treatment,
                futureDate, null);
        assertEquals("Appointment time is required", result);
    }

    @Test
    @DisplayName("Booking passes validation when every field is valid")
    public void validAppointmentPassesValidation() {
        String result = appointmentController.validateAppointment("John Silva", "0771234567", dentist, treatment,
                futureDate, time);
        assertEquals("VALID", result);
    }

    @Test
    @DisplayName("Booking is rejected before touching the database when input is invalid")
    public void registerAppointmentRejectsInvalidInputWithoutTouchingDao() throws SQLException {
        ControllerResult<Appointment> result = appointmentController.registerAppointment("", "Addr",
                "0771234567", "john@example.com", dentist, treatment, futureDate, time, null);

        assertFalse(result.isSuccess());
        assertEquals("Patient name is required", result.getMessage());
    }

    @Test
    @DisplayName("Booking updates the selected existing patient instead of creating a duplicate")
    public void registerAppointmentReusesExistingPatientByContactNumber() throws SQLException {
        Patient existingPatient = new Patient(7, "J. Silva", "Old Address", "0771234567", "old@example.com");
        org.mockito.Mockito.when(patientDAO.findById(7)).thenReturn(existingPatient);

        ControllerResult<Appointment> result = appointmentController.registerAppointment("John Silva", "New Address",
                "0771234567", "new@example.com", dentist, treatment, futureDate, time, 7);

        assertTrue(result.isSuccess());

        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(patientDAO).update(patientCaptor.capture());
        Patient updatedPatient = patientCaptor.getValue();
        assertEquals("John Silva", updatedPatient.getName());
        assertEquals("New Address", updatedPatient.getAddress());
        assertEquals("new@example.com", updatedPatient.getEmail());

        verify(patientDAO, never()).insert(any(Patient.class));
    }

    @Test
    @DisplayName("Booking fails when the selected existing patient no longer exists")
    public void registerAppointmentFailsWhenSelectedPatientNoLongerExists() throws SQLException {
        org.mockito.Mockito.when(patientDAO.findById(7)).thenReturn(null);

        ControllerResult<Appointment> result = appointmentController.registerAppointment("John Silva", "Addr",
                "0771234567", "john@example.com", dentist, treatment, futureDate, time, 7);

        assertFalse(result.isSuccess());
        assertEquals("Selected patient no longer exists.", result.getMessage());
        verify(patientDAO, never()).update(any(Patient.class));
        verify(patientDAO, never()).insert(any(Patient.class));
    }

    @Test
    @DisplayName("Booking a new patient fails when the contact number already belongs to another patient")
    public void registerAppointmentFailsWhenContactNumberAlreadyExistsForNewPatient() throws SQLException {
        Patient existingPatient = new Patient(7, "J. Silva", "Old Address", "0771234567", "old@example.com");
        org.mockito.Mockito.when(patientDAO.findByContactNumber("0771234567")).thenReturn(existingPatient);

        ControllerResult<Appointment> result = appointmentController.registerAppointment("John Silva", "New Address",
                "0771234567", "new@example.com", dentist, treatment, futureDate, time, null);

        assertFalse(result.isSuccess());
        assertEquals("A patient with this contact number already exists.", result.getMessage());
        verify(patientDAO, never()).insert(any(Patient.class));
        verify(patientDAO, never()).update(any(Patient.class));
    }

    @Test
    @DisplayName("Booking fails with a friendly message when the dentist is already booked for that slot")
    public void registerAppointmentReturnsFriendlyMessageOnDoubleBooking() throws SQLException {
        doThrow(new SQLIntegrityConstraintViolationException(
                "Duplicate entry '1-2026-08-31-10:00:00' for key 'appointments.uq_dentist_slot'"))
                .when(appointmentDAO).insert(any(Appointment.class));

        ControllerResult<Appointment> result = appointmentController.registerAppointment("John Silva", "Addr",
                "0771234567", "john@example.com", dentist, treatment, futureDate, time, null);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("already has an appointment"));
    }

    @Test
    @DisplayName("Booking shows a friendly message when a database error occurs")
    public void registerAppointmentReturnsFriendlyMessageOnDatabaseError() throws SQLException {
        org.mockito.Mockito.when(patientDAO.findByContactNumber("0771234567"))
                .thenThrow(new SQLException("Connection refused"));

        ControllerResult<Appointment> result = appointmentController.registerAppointment("John Silva", "Addr",
                "0771234567", "john@example.com", dentist, treatment, futureDate, time, null);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Database error"));
    }

    @Test
    @DisplayName("Searching for an appointment fails when the appointment number does not exist")
    public void searchAppointmentReturnsFailureWhenNotFound() throws SQLException {
        org.mockito.Mockito.when(appointmentDAO.findByAppointmentNo("APT-NOTFOUND")).thenReturn(null);

        ControllerResult<Appointment> result = appointmentController.searchAppointment("APT-NOTFOUND");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("No appointment found"));
    }

    @Test
    @DisplayName("Searching for an appointment shows a friendly message when a database error occurs")
    public void searchAppointmentReturnsFriendlyMessageOnDatabaseError() throws SQLException {
        org.mockito.Mockito.when(appointmentDAO.findByAppointmentNo("APT-ERR"))
                .thenThrow(new SQLException("Connection refused"));

        ControllerResult<Appointment> result = appointmentController.searchAppointment("APT-ERR");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Database error"));
    }

    @Test
    @DisplayName("Updating an appointment reschedules it successfully")
    public void updateAppointmentReschedulesExistingAppointmentSuccessfully() throws SQLException {
        Appointment appointment = existingAppointment();
        Dentist newDentist = new Dentist(2, "Dr. Kamal Silva", "Orthodontics");
        Treatment newTreatment = new Treatment(2, "Cleaning", new BigDecimal("2500.00"));
        LocalDate newDate = futureDate.plusDays(1);
        LocalTime newTime = LocalTime.of(11, 0);

        ControllerResult<Appointment> result = appointmentController.updateAppointment(appointment, newDentist,
                newTreatment, newDate, newTime);

        assertTrue(result.isSuccess());
        assertEquals(newDentist, result.getData().getDentist());
        assertEquals(newTreatment, result.getData().getTreatment());
        assertEquals(newDate, result.getData().getAppointmentDate());
        assertEquals(newTime, result.getData().getAppointmentTime());
        verify(appointmentDAO).update(appointment);
    }

    @Test
    @DisplayName("Updating an appointment into a double-booked slot is rejected with a friendly message")
    public void updateAppointmentReturnsFriendlyMessageOnDoubleBooking() throws SQLException {
        Appointment appointment = existingAppointment();
        doThrow(new SQLIntegrityConstraintViolationException(
                "Duplicate entry '2-2026-08-31-11:00:00' for key 'appointments.uq_dentist_slot'"))
                .when(appointmentDAO).update(any(Appointment.class));

        ControllerResult<Appointment> result = appointmentController.updateAppointment(appointment, dentist,
                treatment, futureDate.plusDays(1), LocalTime.of(11, 0));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("already has an appointment"));
    }

    @Test
    @DisplayName("Updating an appointment shows a friendly message when a database error occurs")
    public void updateAppointmentReturnsFriendlyMessageOnDatabaseError() throws SQLException {
        Appointment appointment = existingAppointment();
        doThrow(new SQLException("Connection refused")).when(appointmentDAO).update(any(Appointment.class));

        ControllerResult<Appointment> result = appointmentController.updateAppointment(appointment, dentist,
                treatment, futureDate.plusDays(1), LocalTime.of(11, 0));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Database error"));
    }

    @Test
    @DisplayName("Cancelling an appointment succeeds")
    public void cancelAppointmentSucceeds() throws SQLException {
        Appointment appointment = existingAppointment();
        org.mockito.Mockito.when(appointmentDAO.findByAppointmentNo(appointment.getAppointmentNo()))
                .thenReturn(appointment);

        ControllerResult<Appointment> result = appointmentController.cancelAppointment(appointment.getAppointmentNo());

        assertTrue(result.isSuccess());
        assertEquals(AppointmentStatus.CANCELLED, result.getData().getStatus());
        verify(appointmentDAO).cancel(appointment.getId());
    }

    @Test
    @DisplayName("Cancelling a non-existent appointment fails with a not-found message")
    public void cancelAppointmentFailsWhenNotFound() throws SQLException {
        org.mockito.Mockito.when(appointmentDAO.findByAppointmentNo("APT-NOTFOUND")).thenReturn(null);

        ControllerResult<Appointment> result = appointmentController.cancelAppointment("APT-NOTFOUND");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("No appointment found"));
    }

    @Test
    @DisplayName("Cancelling an appointment shows a friendly message when a database error occurs")
    public void cancelAppointmentReturnsFriendlyMessageOnDatabaseError() throws SQLException {
        org.mockito.Mockito.when(appointmentDAO.findByAppointmentNo("APT-ERR"))
                .thenThrow(new SQLException("Connection refused"));

        ControllerResult<Appointment> result = appointmentController.cancelAppointment("APT-ERR");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Database error"));
    }

    @Test
    @DisplayName("Listing upcoming appointments returns the scheduled appointments")
    public void listUpcomingReturnsScheduledAppointments() throws SQLException {
        Appointment appointment = existingAppointment();
        org.mockito.Mockito.when(appointmentDAO.findUpcoming()).thenReturn(List.of(appointment));

        ControllerResult<List<Appointment>> result = appointmentController.listUpcoming();

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertEquals(appointment.getAppointmentNo(), result.getData().get(0).getAppointmentNo());
    }

    @Test
    @DisplayName("Listing upcoming appointments shows a friendly message when a database error occurs")
    public void listUpcomingReturnsFriendlyMessageOnDatabaseError() throws SQLException {
        org.mockito.Mockito.when(appointmentDAO.findUpcoming()).thenThrow(new SQLException("Connection refused"));

        ControllerResult<List<Appointment>> result = appointmentController.listUpcoming();

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Database error"));
    }

    @Test
    @DisplayName("Searching appointments with filters returns only the matching appointments")
    public void listAppointmentsAppliesFilterCombination() throws SQLException {
        Appointment appointment = existingAppointment();
        org.mockito.Mockito.when(appointmentDAO.findFiltered(null, futureDate, futureDate.plusDays(7),
                dentist.getId(), "Silva")).thenReturn(List.of(appointment));

        ControllerResult<List<Appointment>> result = appointmentController.listAppointments(null, futureDate,
                futureDate.plusDays(7), dentist.getId(), "Silva");

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("Searching appointments with filters shows a friendly message when a database error occurs")
    public void listAppointmentsReturnsFriendlyMessageOnDatabaseError() throws SQLException {
        org.mockito.Mockito.when(appointmentDAO.findFiltered(any(), any(), any(), any(), any()))
                .thenThrow(new SQLException("Connection refused"));

        ControllerResult<List<Appointment>> result = appointmentController.listAppointments(futureDate, null, null,
                null, null);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Database error"));
    }

    private Appointment existingAppointment() {
        Patient patient = new Patient(1, "John Silva", "Colombo", "0771234567");
        return new Appointment.Builder()
                .id(10)
                .appointmentNo("APT-20260831-001")
                .patient(patient)
                .dentist(dentist)
                .treatment(treatment)
                .appointmentDate(futureDate)
                .appointmentTime(time)
                .build();
    }
}
