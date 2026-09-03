package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Patient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PatientDAOIntegrationTest {

    private final PatientDAO patientDAO = new PatientDAO();
    private final List<Integer> insertedPatientIds = new ArrayList<>();

    @AfterEach
    public void cleanUp() throws SQLException {
        if (!insertedPatientIds.isEmpty()) {
            Connection con = DBConnection.getInstance().getConnection();
            for (int id : insertedPatientIds) {
                try (PreparedStatement pst = con.prepareStatement("DELETE FROM patients WHERE id = ?")) {
                    pst.setInt(1, id);
                    pst.executeUpdate();
                }
            }
        }
    }

    @Test
    @DisplayName("Registering a new patient can be looked up again by id with the same details")
    public void insertThenFindByIdReturnsSamePatient() throws SQLException {
        Patient patient = new Patient();
        patient.setName("Test Patient " + System.nanoTime());
        patient.setAddress("123 Test Lane, Colombo");
        patient.setContactNumber("0770000000");

        int insertedPatientId = patientDAO.insert(patient);
        insertedPatientIds.add(insertedPatientId);
        assertTrue(insertedPatientId > 0);

        Patient found = patientDAO.findById(insertedPatientId);
        assertEquals(patient.getName(), found.getName());
        assertEquals(patient.getAddress(), found.getAddress());
        assertEquals(patient.getContactNumber(), found.getContactNumber());
    }

    @Test
    @DisplayName("Searching by a partial name or contact number returns the matching patient")
    public void searchByNameOrContactNumberReturnsMatchingPatient() throws SQLException {
        Patient patient = new Patient();
        patient.setName("Searchable Patient " + System.nanoTime());
        patient.setAddress("45 Search Street, Colombo");
        patient.setContactNumber("0771234567");

        int id = patientDAO.insert(patient);
        insertedPatientIds.add(id);

        List<Patient> byName = patientDAO.search("Searchable Patient");
        assertTrue(byName.stream().anyMatch(p -> p.getId() == id));

        List<Patient> byContact = patientDAO.search("0771234567");
        assertTrue(byContact.stream().anyMatch(p -> p.getId() == id));
    }

    @Test
    @DisplayName("Searching for a name or contact number that does not exist returns no patients")
    public void searchWithUnknownQueryReturnsEmptyList() throws SQLException {
        List<Patient> results = patientDAO.search("no_such_patient_" + System.nanoTime());
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Searching or looking up by contact number returns only the most recently registered patient when the same number was used twice")
    public void searchAndFindByContactNumberReturnMostRecentPatientForDuplicateContactNumber() throws SQLException {
        String sharedContact = "077" + String.format("%07d", System.nanoTime() % 10_000_000);

        Patient older = new Patient();
        older.setName("Duplicate Contact Older " + System.nanoTime());
        older.setAddress("Old Address");
        older.setContactNumber(sharedContact);
        int olderId = patientDAO.insert(older);
        insertedPatientIds.add(olderId);

        Patient newer = new Patient();
        newer.setName("Duplicate Contact Newer " + System.nanoTime());
        newer.setAddress("New Address");
        newer.setContactNumber(sharedContact);
        int newerId = patientDAO.insert(newer);
        insertedPatientIds.add(newerId);

        Patient foundByContact = patientDAO.findByContactNumber(sharedContact);
        assertEquals(newerId, foundByContact.getId());
        assertEquals(newer.getName(), foundByContact.getName());

        List<Patient> searchResults = patientDAO.search(sharedContact);
        assertTrue(searchResults.stream().anyMatch(p -> p.getId() == newerId));
        assertTrue(searchResults.stream().noneMatch(p -> p.getId() == olderId));
    }

    @Test
    @DisplayName("Updating a patient changes name, address and email but leaves the contact number unchanged")
    public void updateChangesNameAddressAndEmailButNotContactNumber() throws SQLException {
        Patient patient = new Patient();
        patient.setName("Before Update Patient");
        patient.setAddress("Old Address, Colombo");
        patient.setContactNumber("0779000000");
        patient.setEmail("before@example.com");
        int id = patientDAO.insert(patient);
        insertedPatientIds.add(id);

        patient.setId(id);
        patient.setName("After Update Patient");
        patient.setAddress("New Address, Colombo");
        patient.setEmail("after@example.com");
        patient.setContactNumber("0779999999");
        patientDAO.update(patient);

        Patient found = patientDAO.findById(id);
        assertEquals("After Update Patient", found.getName());
        assertEquals("New Address, Colombo", found.getAddress());
        assertEquals("after@example.com", found.getEmail());
        assertEquals("0779000000", found.getContactNumber());
    }
}
