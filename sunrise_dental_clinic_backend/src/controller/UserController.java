package controller;

import dao.UserDAO;
import java.sql.SQLException;
import java.util.List;
import model.Role;
import model.User;
import util.PasswordUtil;

public class UserController {

    private final UserDAO userDAO;

    public UserController() {
        this(new UserDAO());
    }

    public UserController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public ControllerResult<List<User>> listUsers() {
        try {
            return ControllerResult.success(userDAO.findAll());
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }

    public ControllerResult<User> createUser(String username, String password, String roleText) {
        if (username == null || username.trim().isEmpty()) {
            return ControllerResult.failure("Username is required");
        }
        if (password == null || password.trim().isEmpty()) {
            return ControllerResult.failure("Password is required");
        }
        Role role;
        try {
            role = Role.valueOf(roleText);
        } catch (Exception e) {
            return ControllerResult.failure("Role must be ADMIN or RECEPTIONIST");
        }
        try {
            if (userDAO.findByUsername(username.trim()) != null) {
                return ControllerResult.failure("Username already exists");
            }
            User user = new User();
            user.setUsername(username.trim());
            user.setPasswordHash(PasswordUtil.hash(password));
            user.setRole(role);
            user.setActive(true);
            userDAO.insert(user);
            return ControllerResult.success(user);
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }

    public ControllerResult<User> setActive(User target, boolean active) {
        try {
            userDAO.updateActive(target.getId(), active);
            target.setActive(active);
            return ControllerResult.success(target);
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }
}
