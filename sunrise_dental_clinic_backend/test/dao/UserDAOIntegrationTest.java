package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import model.Role;
import model.User;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.PasswordUtil;

/**
 * Integration test against the real local MySQL "sunrise_dental_clinic"
 * database (already created and seeded per PROJECT_PLAN). The lookups
 * against the seeded admin row are read-only; tests that exercise
 * insert/findAll/updateActive create a throwaway user account and delete it
 * again in @AfterEach.
 */
public class UserDAOIntegrationTest {

    private final UserDAO userDAO = new UserDAO();
    private int insertedUserId = 0;

    @AfterEach
    public void cleanUp() throws SQLException {
        if (insertedUserId != 0) {
            Connection con = DBConnection.getInstance().getConnection();
            try (PreparedStatement pst = con.prepareStatement("DELETE FROM users WHERE id = ?")) {
                pst.setInt(1, insertedUserId);
                pst.executeUpdate();
            }
        }
    }

    @Test
    @DisplayName("The seeded admin account exists with the admin role and the correct password")
    public void findsSeededAdminUserWithCorrectRoleAndPassword() throws SQLException {
        User admin = userDAO.findByUsername("admin");

        assertEquals("admin", admin.getUsername());
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(PasswordUtil.matches("admin123", admin.getPasswordHash()));
    }

    @Test
    @DisplayName("Looking up a username that does not exist returns nothing")
    public void unknownUsernameReturnsNull() throws SQLException {
        User user = userDAO.findByUsername("this_username_does_not_exist");
        assertNull(user);
    }

    @Test
    @DisplayName("Registering a new staff account can be looked up again by id with its hashed password intact")
    public void insertThenFindByIdReturnsSameUser() throws SQLException {
        User user = new User();
        user.setUsername("test.user." + System.nanoTime());
        user.setPasswordHash(PasswordUtil.hash("Secret123!"));
        user.setRole(Role.RECEPTIONIST);
        user.setActive(true);

        insertedUserId = userDAO.insert(user);
        assertTrue(insertedUserId > 0);

        User found = userDAO.findById(insertedUserId);
        assertEquals(user.getUsername(), found.getUsername());
        assertEquals(Role.RECEPTIONIST, found.getRole());
        assertTrue(found.isActive());
        assertTrue(PasswordUtil.matches("Secret123!", found.getPasswordHash()));
    }

    @Test
    @DisplayName("The list of all staff accounts includes a newly registered user")
    public void findAllIncludesNewlyInsertedUser() throws SQLException {
        User user = new User();
        user.setUsername("test.findall." + System.nanoTime());
        user.setPasswordHash(PasswordUtil.hash("Secret123!"));
        user.setRole(Role.ADMIN);
        user.setActive(true);
        insertedUserId = userDAO.insert(user);

        List<User> users = userDAO.findAll();

        assertTrue(users.stream().anyMatch(u -> u.getId() == insertedUserId));
    }

    @Test
    @DisplayName("Deactivating and reactivating a staff account updates whether it can log in")
    public void updateActiveTogglesAccountBetweenActiveAndInactive() throws SQLException {
        User user = new User();
        user.setUsername("test.active." + System.nanoTime());
        user.setPasswordHash(PasswordUtil.hash("Secret123!"));
        user.setRole(Role.RECEPTIONIST);
        user.setActive(true);
        insertedUserId = userDAO.insert(user);

        userDAO.updateActive(insertedUserId, false);
        assertFalse(userDAO.findById(insertedUserId).isActive());

        userDAO.updateActive(insertedUserId, true);
        assertTrue(userDAO.findById(insertedUserId).isActive());
    }
}
