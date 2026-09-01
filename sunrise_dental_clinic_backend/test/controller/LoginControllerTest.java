package controller;

import dao.UserDAO;
import java.sql.SQLException;
import model.Role;
import model.User;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
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
    public void emptyUsernameFailsValidation() {
        ControllerResult<User> result = loginController.login("", "somePassword");
        assertFalse(result.isSuccess());
        assertEquals("Username is required", result.getMessage());
    }

    @Test
    public void emptyPasswordFailsValidation() {
        ControllerResult<User> result = loginController.login("admin", "");
        assertFalse(result.isSuccess());
        assertEquals("Password is required", result.getMessage());
    }

    @Test
    public void unknownUsernameFailsWithInvalidCredentialsMessage() throws SQLException {
        when(userDAO.findByUsername("ghost")).thenReturn(null);

        ControllerResult<User> result = loginController.login("ghost", "anyPassword");

        assertFalse(result.isSuccess());
        assertEquals("Invalid username or password", result.getMessage());
    }

    @Test
    public void wrongPasswordFailsWithInvalidCredentialsMessage() throws SQLException {
        User admin = new User(1, "admin", PasswordUtil.hash("admin123"), Role.ADMIN);
        when(userDAO.findByUsername("admin")).thenReturn(admin);

        ControllerResult<User> result = loginController.login("admin", "wrongPassword");

        assertFalse(result.isSuccess());
        assertEquals("Invalid username or password", result.getMessage());
    }

    @Test
    public void correctCredentialsLogInSuccessfully() throws SQLException {
        User admin = new User(1, "admin", PasswordUtil.hash("admin123"), Role.ADMIN);
        when(userDAO.findByUsername("admin")).thenReturn(admin);

        ControllerResult<User> result = loginController.login("admin", "admin123");

        assertTrue(result.isSuccess());
        assertEquals(Role.ADMIN, result.getData().getRole());
    }
}
