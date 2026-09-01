package controller;

import dao.AppointmentDAO;
import dao.BillDAO;
import dao.TreatmentDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import model.Appointment;
import model.Bill;
import model.Dentist;
import model.Patient;
import model.Treatment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BillingControllerTest {

    private AppointmentDAO appointmentDAO;
    private TreatmentDAO treatmentDAO;
    private BillDAO billDAO;
    private BillingController billingController;

    @BeforeEach
    public void setUp() {
        appointmentDAO = mock(AppointmentDAO.class);
        treatmentDAO = mock(TreatmentDAO.class);
        billDAO = mock(BillDAO.class);
        billingController = new BillingController(appointmentDAO, treatmentDAO, billDAO);
    }

    @Test
    public void calculateTotalAddsConsultationAndTreatmentFees() {
        BigDecimal total = billingController.calculateTotal(new BigDecimal("1500.00"), new BigDecimal("4500.00"));
        assertEquals(0, new BigDecimal("6000.00").compareTo(total));
    }

    @Test
    public void calculateTotalHandlesZeroTreatmentFeeBoundary() {
        BigDecimal total = billingController.calculateTotal(new BigDecimal("1500.00"), BigDecimal.ZERO);
        assertEquals(0, new BigDecimal("1500.00").compareTo(total));
    }

    @Test
    public void generateBillFailsWhenAppointmentNotFound() throws SQLException {
        when(appointmentDAO.findByAppointmentNo("APT-MISSING")).thenReturn(null);

        ControllerResult<String> result = billingController.generateBill("APT-MISSING");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("No appointment found"));
    }

    @Test
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
}
