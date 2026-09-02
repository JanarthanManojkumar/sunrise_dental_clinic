package controller;

import dao.UserDAO;
import java.sql.SQLException;
import model.User;
import util.PasswordUtil;
import util.SessionManager;

public class LoginController {

    private final UserDAO userDAO;

    public LoginController() {
        this(new UserDAO());
    }

    public LoginController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public String validateInput(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return "Username is required";
        }
        if (password == null || password.trim().isEmpty()) {
            return "Password is required";
        }
        return "VALID";
    }

    public ControllerResult<User> login(String username, String password) {
        String validation = validateInput(username, password);
        if (!validation.equals("VALID")) {
            return ControllerResult.failure(validation);
        }
        try {
            User user = userDAO.findByUsername(username);
            if (user == null || !PasswordUtil.matches(password, user.getPasswordHash())) {
                return ControllerResult.failure("Invalid username or password");
            }
            if (!user.isActive()) {
                return ControllerResult.failure("This account has been deactivated.");
            }
            SessionManager.getInstance().startSession(user);
            return ControllerResult.success(user);
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }
}
