package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import model.Patient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class PatientDAOIntegrationTest {

    private final PatientDAO patientDAO = new PatientDAO();
    private int insertedPatientId = 0;

    @AfterEach
    public void cleanUp() throws SQLException {
        if (insertedPatientId != 0) {
            Connection con = DBConnection.getInstance().getConnection();
            try (PreparedStatement pst = con.prepareStatement("DELETE FROM patients WHERE id = ?")) {
                pst.setInt(1, insertedPatientId);
                pst.executeUpdate();
            }
        }
    }

    @Test
    public void insertThenFindByIdReturnsSamePatient() throws SQLException {
        Patient patient = new Patient();
        patient.setName("Test Patient " + System.nanoTime());
        patient.setAddress("123 Test Lane, Colombo");
        patient.setContactNumber("0770000000");

        insertedPatientId = patientDAO.insert(patient);
        assertTrue(insertedPatientId > 0);

        Patient found = patientDAO.findById(insertedPatientId);
        assertEquals(patient.getName(), found.getName());
        assertEquals(patient.getAddress(), found.getAddress());
        assertEquals(patient.getContactNumber(), found.getContactNumber());
    }
}
