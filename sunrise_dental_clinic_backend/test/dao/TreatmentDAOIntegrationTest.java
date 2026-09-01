package dao;

import db.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import model.Treatment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class TreatmentDAOIntegrationTest {

    private final TreatmentDAO treatmentDAO = new TreatmentDAO();
    private int insertedTreatmentId = 0;

    @AfterEach
    public void cleanUp() throws SQLException {
        if (insertedTreatmentId != 0) {
            Connection con = DBConnection.getInstance().getConnection();
            try (PreparedStatement pst = con.prepareStatement("DELETE FROM treatments WHERE id = ?")) {
                pst.setInt(1, insertedTreatmentId);
                pst.executeUpdate();
            }
        }
    }

    @Test
    public void insertThenFindByIdAndFindByNameReturnSameTreatment() throws SQLException {
        String name = "Test Treatment " + System.nanoTime();
        Treatment treatment = new Treatment();
        treatment.setName(name);
        treatment.setFee(new BigDecimal("2500.00"));

        insertedTreatmentId = treatmentDAO.insert(treatment);
        assertTrue(insertedTreatmentId > 0);

        Treatment byId = treatmentDAO.findById(insertedTreatmentId);
        assertEquals(name, byId.getName());
        assertEquals(0, new BigDecimal("2500.00").compareTo(byId.getFee()));

        Treatment byName = treatmentDAO.findByName(name);
        assertEquals(insertedTreatmentId, byName.getId());
    }

    @Test
    public void updateChangesNameAndFee() throws SQLException {
        Treatment treatment = new Treatment();
        treatment.setName("Before Update Treatment " + System.nanoTime());
        treatment.setFee(new BigDecimal("1000.00"));
        insertedTreatmentId = treatmentDAO.insert(treatment);

        treatment.setId(insertedTreatmentId);
        treatment.setName("After Update Treatment");
        treatment.setFee(new BigDecimal("3000.00"));
        treatmentDAO.update(treatment);

        Treatment found = treatmentDAO.findById(insertedTreatmentId);
        assertEquals("After Update Treatment", found.getName());
        assertEquals(0, new BigDecimal("3000.00").compareTo(found.getFee()));
    }

    @Test
    public void seededConsultationTreatmentExistsForBillingFlatFee() throws SQLException {
        Treatment consultation = treatmentDAO.findByName("Consultation");
        assertEquals("Consultation", consultation.getName());
        assertTrue(consultation.getFee().compareTo(BigDecimal.ZERO) > 0);
    }
}
