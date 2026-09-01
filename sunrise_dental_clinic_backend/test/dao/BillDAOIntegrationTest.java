package dao;

import db.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import model.Appointment;
import model.Bill;
import model.Dentist;
import model.Patient;
import model.Treatment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
                .appointmentNo("APT-BILLTEST-" + System.nanoTime())
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
}
