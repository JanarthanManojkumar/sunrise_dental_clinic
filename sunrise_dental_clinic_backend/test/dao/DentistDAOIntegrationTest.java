package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import model.Dentist;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class DentistDAOIntegrationTest {

    private final DentistDAO dentistDAO = new DentistDAO();
    private int insertedDentistId = 0;

    @AfterEach
    public void cleanUp() throws SQLException {
        if (insertedDentistId != 0) {
            Connection con = DBConnection.getInstance().getConnection();
            try (PreparedStatement pst = con.prepareStatement("DELETE FROM dentists WHERE id = ?")) {
                pst.setInt(1, insertedDentistId);
                pst.executeUpdate();
            }
        }
    }

    @Test
    public void insertThenFindByIdReturnsSameDentist() throws SQLException {
        Dentist dentist = new Dentist();
        dentist.setName("Dr. Test " + System.nanoTime());
        dentist.setSpecialization("Orthodontics");

        insertedDentistId = dentistDAO.insert(dentist);
        assertTrue(insertedDentistId > 0);

        Dentist found = dentistDAO.findById(insertedDentistId);
        assertEquals(dentist.getName(), found.getName());
        assertEquals("Orthodontics", found.getSpecialization());
    }

    @Test
    public void findAllIncludesNewlyInsertedDentist() throws SQLException {
        Dentist dentist = new Dentist();
        dentist.setName("Dr. FindAll Test " + System.nanoTime());
        dentist.setSpecialization("Oral Surgery");
        insertedDentistId = dentistDAO.insert(dentist);

        boolean present = dentistDAO.findAll().stream()
                .anyMatch(d -> d.getId() == insertedDentistId);

        assertTrue(present);
    }

    @Test
    public void updateChangesNameAndSpecialization() throws SQLException {
        Dentist dentist = new Dentist();
        dentist.setName("Dr. Before Update");
        dentist.setSpecialization("General Dentistry");
        insertedDentistId = dentistDAO.insert(dentist);

        dentist.setId(insertedDentistId);
        dentist.setName("Dr. After Update");
        dentist.setSpecialization("Periodontics");
        dentistDAO.update(dentist);

        Dentist found = dentistDAO.findById(insertedDentistId);
        assertEquals("Dr. After Update", found.getName());
        assertEquals("Periodontics", found.getSpecialization());
    }
}
