package controller;

import dao.AppointmentDAO;
import dao.BillDAO;
import dao.TreatmentDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import model.Appointment;
import model.AppointmentStatus;
import model.Bill;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import util.EmailException;
import util.EmailService;

@ExtendWith(MockitoExtension.class)
public class BillingControllerTest {

    private AppointmentDAO appointmentDAO;
    private TreatmentDAO treatmentDAO;
    private BillDAO billDAO;
    private EmailService emailService;
    private BillingController billingController;

    @BeforeEach
    public void setUp() {
        appointmentDAO = mock(AppointmentDAO.class);
        treatmentDAO = mock(TreatmentDAO.class);
        billDAO = mock(BillDAO.class);
        emailService = mock(EmailService.class);
        billingController = new BillingController(appointmentDAO, treatmentDAO, billDAO, emailService);
    }

    @Test
    @DisplayName("Bill total adds the consultation fee and the treatment fee")
    public void calculateTotalAddsConsultationAndTreatmentFees() {
        BigDecimal total = billingController.calculateTotal(new BigDecimal("1500.00"), new BigDecimal("4500.00"));
        assertEquals(0, new BigDecimal("6000.00").compareTo(total));
    }

    @Test
    @DisplayName("Bill total equals the consultation fee alone when the treatment fee is zero")
    public void calculateTotalHandlesZeroTreatmentFeeBoundary() {
        BigDecimal total = billingController.calculateTotal(new BigDecimal("1500.00"), BigDecimal.ZERO);
        assertEquals(0, new BigDecimal("1500.00").compareTo(total));
    }

    @Test
    @DisplayName("Generating a bill fails when the appointment number does not exist")
    public void generateBillFailsWhenAppointmentNotFound() throws SQLException {
        when(appointmentDAO.findByAppointmentNo("APT-MISSING")).thenReturn(null);

        ControllerResult<String> result = billingController.generateBill("APT-MISSING");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("No appointment found"));
    }

    @Test
    @DisplayName("Generating a bill fails for a cancelled appointment")
    public void generateBillFailsForCancelledAppointment() throws SQLException {
        Appointment appointment = cancelledAppointment();
        when(appointmentDAO.findByAppointmentNo("APT-20260831-002")).thenReturn(appointment);

        ControllerResult<String> result = billingController.generateBill("APT-20260831-002");

        assertFalse(result.isSuccess());
        assertEquals("Cannot bill a cancelled appointment.", result.getMessage());
    }

    @Test
    @DisplayName("Generating a bill fails when the consultation fee is not configured in the price list")
    public void generateBillFailsWhenConsultationFeeNotConfigured() throws SQLException {
        Appointment appointment = sampleAppointment();
        when(appointmentDAO.findByAppointmentNo("APT-20260831-001")).thenReturn(appointment);
        when(billDAO.findByAppointmentId(appointment.getId())).thenReturn(null);
        when(treatmentDAO.findByName("Consultation")).thenReturn(null);

        ControllerResult<String> result = billingController.generateBill("APT-20260831-001");

        assertFalse(result.isSuccess());
        assertEquals("Consultation fee is not configured in the price list.", result.getMessage());
    }

    @Test
    @DisplayName("Generating a bill computes the total from the consultation fee and treatment fee")
    public void generateBillComputesTotalFromConsultationAndTreatmentFee() throws SQLException {
        Appointment appointment = sampleAppointment();
        when(appointmentDAO.findByAppointmentNo("APT-20260831-001")).thenReturn(appointment);
        when(billDAO.findByAppointmentId(appointment.getId())).thenReturn(null);
        when(treatmentDAO.findByName("Consultation"))
                .thenReturn(new Treatment(99, "Consultation", new BigDecimal("1500.00")));

        ControllerResult<String> result = billingController.generateBill("APT-20260831-001");

        assertTrue(result.isSuccess());
        assertTrue(result.getData().contains("6,000.00"));
    }

    @Test
    @DisplayName("Generating a bill for an already-billed appointment reuses the existing receipt")
    public void generateBillReturnsExistingReceiptWithoutRecalculating() throws SQLException {
        Appointment appointment = sampleAppointment();
        Bill existingBill = new Bill(5, appointment.getId(), new BigDecimal("1500.00"),
                new BigDecimal("4500.00"), new BigDecimal("6000.00"), java.time.LocalDateTime.now());
        when(appointmentDAO.findByAppointmentNo("APT-20260831-001")).thenReturn(appointment);
        when(billDAO.findByAppointmentId(appointment.getId())).thenReturn(existingBill);

        ControllerResult<String> result = billingController.generateBill("APT-20260831-001");

        assertTrue(result.isSuccess());
        assertTrue(result.getData().contains("6,000.00"));
    }

    @Test
    @DisplayName("Generating a bill shows a friendly message when a database error occurs")
    public void generateBillReturnsFriendlyMessageOnDatabaseError() throws SQLException {
        when(appointmentDAO.findByAppointmentNo("APT-20260831-001"))
                .thenThrow(new SQLException("Connection refused"));

        ControllerResult<String> result = billingController.generateBill("APT-20260831-001");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Database error"));
    }

    @Test
    @DisplayName("Emailing a bill fails when the appointment number does not exist")
    public void emailBillFailsWhenAppointmentNotFound() throws SQLException {
        when(appointmentDAO.findByAppointmentNo("APT-MISSING")).thenReturn(null);

        ControllerResult<String> result = billingController.emailBill("APT-MISSING");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("No appointment found"));
    }

    @Test
    @DisplayName("Emailing a bill fails when the patient has no email address on file")
    public void emailBillFailsWhenPatientHasNoEmailOnFile() throws SQLException {
        Appointment appointment = sampleAppointment();
        when(appointmentDAO.findByAppointmentNo("APT-20260831-001")).thenReturn(appointment);

        ControllerResult<String> result = billingController.emailBill("APT-20260831-001");

        assertFalse(result.isSuccess());
        assertEquals("This patient does not have an email address on file.", result.getMessage());
    }

    @Test
    @DisplayName("Emailing a bill fails when the bill has not been generated yet")
    public void emailBillFailsWhenBillNotYetGenerated() throws SQLException {
        Appointment appointment = sampleAppointmentWithEmail("john@example.com");
        when(appointmentDAO.findByAppointmentNo("APT-20260831-001")).thenReturn(appointment);
        when(billDAO.findByAppointmentId(appointment.getId())).thenReturn(null);

        ControllerResult<String> result = billingController.emailBill("APT-20260831-001");

        assertFalse(result.isSuccess());
        assertEquals("Bill has not been generated yet.", result.getMessage());
    }

    @Test
    @DisplayName("Emailing a bill succeeds when the patient has an email on file")
    public void emailBillSendsSuccessfully() throws SQLException, EmailException {
        Appointment appointment = sampleAppointmentWithEmail("john@example.com");
        Bill existingBill = new Bill(5, appointment.getId(), new BigDecimal("1500.00"),
                new BigDecimal("4500.00"), new BigDecimal("6000.00"), java.time.LocalDateTime.now());
        when(appointmentDAO.findByAppointmentNo("APT-20260831-001")).thenReturn(appointment);
        when(billDAO.findByAppointmentId(appointment.getId())).thenReturn(existingBill);

        ControllerResult<String> result = billingController.emailBill("APT-20260831-001");

        assertTrue(result.isSuccess());
        assertEquals("Bill emailed to john@example.com", result.getData());
        verify(emailService).sendBillEmail(eq("john@example.com"), eq("John Silva"), anyString(), anyString());
    }

    @Test
    @DisplayName("Emailing a bill shows a friendly message when the email fails to send")
    public void emailBillReturnsFriendlyMessageWhenSendFails() throws SQLException, EmailException {
        Appointment appointment = sampleAppointmentWithEmail("john@example.com");
        Bill existingBill = new Bill(5, appointment.getId(), new BigDecimal("1500.00"),
                new BigDecimal("4500.00"), new BigDecimal("6000.00"), java.time.LocalDateTime.now());
        when(appointmentDAO.findByAppointmentNo("APT-20260831-001")).thenReturn(appointment);
        when(billDAO.findByAppointmentId(appointment.getId())).thenReturn(existingBill);
        doThrow(new EmailException("SMTP connection timed out"))
                .when(emailService).sendBillEmail(anyString(), anyString(), anyString(), anyString());

        ControllerResult<String> result = billingController.emailBill("APT-20260831-001");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Email error"));
    }

    @Test
    @DisplayName("Emailing a bill shows a friendly message when a database error occurs")
    public void emailBillReturnsFriendlyMessageOnDatabaseError() throws SQLException {
        when(appointmentDAO.findByAppointmentNo("APT-20260831-001"))
                .thenThrow(new SQLException("Connection refused"));

        ControllerResult<String> result = billingController.emailBill("APT-20260831-001");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Database error"));
    }

    private Appointment sampleAppointment() {
        Patient patient = new Patient(1, "John Silva", "Colombo", "0771234567");
        Dentist dentist = new Dentist(1, "Dr. Nimal Perera", "General Dentistry");
        Treatment treatment = new Treatment(1, "Filling", new BigDecimal("4500.00"));
        return new Appointment.Builder()
                .id(10)
                .appointmentNo("APT-20260831-001")
                .patient(patient)
                .dentist(dentist)
                .treatment(treatment)
                .appointmentDate(LocalDate.now().plusYears(5))
                .appointmentTime(LocalTime.of(10, 0))
                .build();
    }

    private Appointment cancelledAppointment() {
        Patient patient = new Patient(1, "John Silva", "Colombo", "0771234567");
        Dentist dentist = new Dentist(1, "Dr. Nimal Perera", "General Dentistry");
        Treatment treatment = new Treatment(1, "Filling", new BigDecimal("4500.00"));
        return new Appointment.Builder()
                .id(11)
                .appointmentNo("APT-20260831-002")
                .patient(patient)
                .dentist(dentist)
                .treatment(treatment)
                .appointmentDate(LocalDate.now().plusYears(5))
                .appointmentTime(LocalTime.of(10, 0))
                .status(AppointmentStatus.CANCELLED)
                .build();
    }

    private Appointment sampleAppointmentWithEmail(String email) {
        Appointment appointment = sampleAppointment();
        appointment.getPatient().setEmail(email);
        return appointment;
    }
}
