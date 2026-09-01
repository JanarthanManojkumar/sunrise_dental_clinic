package dao;

import java.sql.SQLException;
import model.Role;
import model.User;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import util.PasswordUtil;

/**
 * Integration test against the real local MySQL "sunrise_dental_clinic"
 * database (already created and seeded per PROJECT_PLAN). Read-only: does
 * not insert or delete any rows.
 */
public class UserDAOIntegrationTest {

    private final UserDAO userDAO = new UserDAO();

    @Test
    public void findsSeededAdminUserWithCorrectRoleAndPassword() throws SQLException {
        User admin = userDAO.findByUsername("admin");

        assertEquals("admin", admin.getUsername());
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(PasswordUtil.matches("admin123", admin.getPasswordHash()));
    }

    @Test
    public void unknownUsernameReturnsNull() throws SQLException {
        User user = userDAO.findByUsername("this_username_does_not_exist");
        assertNull(user);
    }
}
