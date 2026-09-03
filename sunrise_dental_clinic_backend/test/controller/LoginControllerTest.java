package controller;

import dao.UserDAO;
import java.sql.SQLException;
import model.Role;
import model.User;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import util.PasswordUtil;

@ExtendWith(MockitoExtension.class)
public class LoginControllerTest {

    private UserDAO userDAO;
    private LoginController loginController;

    @BeforeEach
    public void setUp() {
        userDAO = mock(UserDAO.class);
        loginController = new LoginController(userDAO);
    }

    @Test
    @DisplayName("Login fails when username is empty")
    public void emptyUsernameFailsValidation() {
        ControllerResult<User> result = loginController.login("", "somePassword");
        assertFalse(result.isSuccess());
        assertEquals("Username is required", result.getMessage());
    }

    @Test
    @DisplayName("Login fails when username is only whitespace")
    public void whitespaceUsernameFailsValidation() {
        ControllerResult<User> result = loginController.login("   ", "somePassword");
        assertFalse(result.isSuccess());
        assertEquals("Username is required", result.getMessage());
    }

    @Test
    @DisplayName("Login fails when password is empty")
    public void emptyPasswordFailsValidation() {
        ControllerResult<User> result = loginController.login("admin", "");
        assertFalse(result.isSuccess());
        assertEquals("Password is required", result.getMessage());
    }

    @Test
    @DisplayName("Login fails when password is only whitespace")
    public void whitespacePasswordFailsValidation() {
        ControllerResult<User> result = loginController.login("admin", "   ");
        assertFalse(result.isSuccess());
        assertEquals("Password is required", result.getMessage());
    }

    @Test
    @DisplayName("Login fails with invalid credentials message when the username does not exist")
    public void unknownUsernameFailsWithInvalidCredentialsMessage() throws SQLException {
        when(userDAO.findByUsername("ghost")).thenReturn(null);

        ControllerResult<User> result = loginController.login("ghost", "anyPassword");

        assertFalse(result.isSuccess());
        assertEquals("Invalid username or password", result.getMessage());
    }

    @Test
    @DisplayName("Login fails with invalid credentials message when the password is wrong")
    public void wrongPasswordFailsWithInvalidCredentialsMessage() throws SQLException {
        User admin = new User(1, "admin", PasswordUtil.hash("admin123"), Role.ADMIN, true);
        when(userDAO.findByUsername("admin")).thenReturn(admin);

        ControllerResult<User> result = loginController.login("admin", "wrongPassword");

        assertFalse(result.isSuccess());
        assertEquals("Invalid username or password", result.getMessage());
    }

    @Test
    @DisplayName("Login fails when the account has been deactivated")
    public void deactivatedAccountFailsLogin() throws SQLException {
        User inactiveAdmin = new User(1, "admin", PasswordUtil.hash("admin123"), Role.ADMIN, false);
        when(userDAO.findByUsername("admin")).thenReturn(inactiveAdmin);

        ControllerResult<User> result = loginController.login("admin", "admin123");

        assertFalse(result.isSuccess());
        assertEquals("This account has been deactivated.", result.getMessage());
    }

    @Test
    @DisplayName("Login shows a friendly message when a database error occurs")
    public void sqlExceptionDuringLoginIsHandledGracefully() throws SQLException {
        when(userDAO.findByUsername("admin")).thenThrow(new SQLException("Connection refused"));

        ControllerResult<User> result = loginController.login("admin", "admin123");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Database error"));
    }

    @Test
    @DisplayName("Login succeeds with the correct username and password")
    public void correctCredentialsLogInSuccessfully() throws SQLException {
        User admin = new User(1, "admin", PasswordUtil.hash("admin123"), Role.ADMIN, true);
        when(userDAO.findByUsername("admin")).thenReturn(admin);

        ControllerResult<User> result = loginController.login("admin", "admin123");

        assertTrue(result.isSuccess());
        assertEquals(Role.ADMIN, result.getData().getRole());
    }

    @Test
    @DisplayName("A receptionist can log in successfully")
    public void receptionistRoleLogsInSuccessfully() throws SQLException {
        User receptionist = new User(2, "reception", PasswordUtil.hash("recep123"), Role.RECEPTIONIST, true);
        when(userDAO.findByUsername("reception")).thenReturn(receptionist);

        ControllerResult<User> result = loginController.login("reception", "recep123");

        assertTrue(result.isSuccess());
        assertEquals(Role.RECEPTIONIST, result.getData().getRole());
    }
}
