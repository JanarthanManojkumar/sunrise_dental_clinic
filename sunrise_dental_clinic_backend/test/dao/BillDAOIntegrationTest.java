package dao;

import db.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import model.Appointment;
import model.AppointmentStatus;
import model.Bill;
import model.Dentist;
import model.DentistRevenue;
import model.Patient;
import model.Treatment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BillDAOIntegrationTest {

    private final BillDAO billDAO = new BillDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DentistDAO dentistDAO = new DentistDAO();
    private final TreatmentDAO treatmentDAO = new TreatmentDAO();

    private Dentist fixtureDentist;
    private Treatment fixtureTreatment;
    private Patient fixturePatient;
    private Appointment fixtureAppointment;
    private final List<Integer> extraAppointmentIds = new ArrayList<>();

    @BeforeEach
    public void setUp() throws SQLException {
        fixtureDentist = new Dentist();
        fixtureDentist.setName("Dr. Bill Fixture " + System.nanoTime());
        fixtureDentist.setSpecialization("General Dentistry");
        dentistDAO.insert(fixtureDentist);

        fixtureTreatment = new Treatment();
        fixtureTreatment.setName("Bill Fixture Treatment " + System.nanoTime());
        fixtureTreatment.setFee(new BigDecimal("4500.00"));
        treatmentDAO.insert(fixtureTreatment);

        fixturePatient = new Patient();
        fixturePatient.setName("Bill Fixture Patient");
        fixturePatient.setAddress("Test Address");
        fixturePatient.setContactNumber("0770000002");
        patientDAO.insert(fixturePatient);

        fixtureAppointment = new Appointment.Builder()
                .appointmentNo("APT-BILLTEST-" + (System.nanoTime() % 1_000_000L))
                .patient(fixturePatient)
                .dentist(fixtureDentist)
                .treatment(fixtureTreatment)
                .appointmentDate(LocalDate.now().plusYears(5))
                .appointmentTime(LocalTime.of(16, 0))
                .build();
        appointmentDAO.insert(fixtureAppointment);
    }

    @AfterEach
    public void cleanUp() throws SQLException {
        Connection con = DBConnection.getInstance().getConnection();
        for (int id : extraAppointmentIds) {
            try (PreparedStatement pst = con.prepareStatement("DELETE FROM bills WHERE appointment_id = ?")) {
                pst.setInt(1, id);
                pst.executeUpdate();
            }
            try (PreparedStatement pst = con.prepareStatement("DELETE FROM appointments WHERE id = ?")) {
                pst.setInt(1, id);
                pst.executeUpdate();
            }
        }
        try (PreparedStatement pst = con.prepareStatement("DELETE FROM bills WHERE appointment_id = ?")) {
            pst.setInt(1, fixtureAppointment.getId());
            pst.executeUpdate();
        }
        try (PreparedStatement pst = con.prepareStatement("DELETE FROM appointments WHERE id = ?")) {
            pst.setInt(1, fixtureAppointment.getId());
            pst.executeUpdate();
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

    @Test
    @DisplayName("Issuing a bill for an appointment can be found again with the correct total")
    public void insertThenFindByAppointmentIdReturnsSameBill() throws SQLException {
        Bill bill = new Bill();
        bill.setAppointmentId(fixtureAppointment.getId());
        bill.setConsultationFee(new BigDecimal("1500.00"));
        bill.setTreatmentFee(new BigDecimal("4500.00"));
        bill.setTotal(new BigDecimal("6000.00"));
        bill.setIssuedAt(java.time.LocalDateTime.now());

        billDAO.insert(bill);

        Bill found = billDAO.findByAppointmentId(fixtureAppointment.getId());
        assertEquals(0, new BigDecimal("6000.00").compareTo(found.getTotal()));
    }

    @Test
    @DisplayName("An appointment that has not been billed yet has no bill on file")
    public void findByAppointmentIdWithNoBillReturnsNull() throws SQLException {
        Bill found = billDAO.findByAppointmentId(fixtureAppointment.getId());
        assertNull(found);
    }

    @Test
    @DisplayName("Revenue per dentist report includes the billed total for a dentist")
    public void revenuePerDentistIncludesFixtureDentistTotal() throws SQLException {
        Bill bill = new Bill();
        bill.setAppointmentId(fixtureAppointment.getId());
        bill.setConsultationFee(new BigDecimal("1500.00"));
        bill.setTreatmentFee(new BigDecimal("4500.00"));
        bill.setTotal(new BigDecimal("6000.00"));
        bill.setIssuedAt(java.time.LocalDateTime.now());
        billDAO.insert(bill);

        boolean present = billDAO.revenuePerDentist().stream()
                .anyMatch(r -> r.dentistName().equals(fixtureDentist.getName())
                        && r.revenue().compareTo(new BigDecimal("6000.00")) == 0);

        assertTrue(present);
    }

    @Test
    @DisplayName("Revenue per dentist excludes the bill for a cancelled appointment")
    public void revenuePerDentistExcludesCancelledAppointmentBill() throws SQLException {
        Bill activeBill = new Bill();
        activeBill.setAppointmentId(fixtureAppointment.getId());
        activeBill.setConsultationFee(new BigDecimal("1500.00"));
        activeBill.setTreatmentFee(new BigDecimal("4500.00"));
        activeBill.setTotal(new BigDecimal("6000.00"));
        activeBill.setIssuedAt(java.time.LocalDateTime.now());
        billDAO.insert(activeBill);

        Appointment cancelledAppointment = new Appointment.Builder()
                .appointmentNo("APT-BILLTEST-C-" + (System.nanoTime() % 1_000_000L))
                .patient(fixturePatient)
                .dentist(fixtureDentist)
                .treatment(fixtureTreatment)
                .appointmentDate(LocalDate.now().plusYears(5))
                .appointmentTime(LocalTime.of(17, 0))
                .status(AppointmentStatus.CANCELLED)
                .build();
        int cancelledAppointmentId = appointmentDAO.insert(cancelledAppointment);
        extraAppointmentIds.add(cancelledAppointmentId);

        Bill cancelledBill = new Bill();
        cancelledBill.setAppointmentId(cancelledAppointmentId);
        cancelledBill.setConsultationFee(new BigDecimal("1500.00"));
        cancelledBill.setTreatmentFee(new BigDecimal("9000.00"));
        cancelledBill.setTotal(new BigDecimal("10500.00"));
        cancelledBill.setIssuedAt(java.time.LocalDateTime.now());
        billDAO.insert(cancelledBill);

        BigDecimal revenueForDentist = billDAO.revenuePerDentist().stream()
                .filter(r -> r.dentistName().equals(fixtureDentist.getName()))
                .map(DentistRevenue::revenue)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        assertEquals(0, new BigDecimal("6000.00").compareTo(revenueForDentist));
    }
}
