package api;

import com.sun.net.httpserver.HttpExchange;
import controller.ControllerResult;
import controller.UserController;
import dao.UserDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import model.User;
import org.json.JSONObject;

/** Handles /api/users (list/create, admin-only) and /api/users/{id} (activate/deactivate, admin-only). */
public class UserHandler extends BaseHandler {

    private final UserController userController = new UserController();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void route(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String[] segments = subPath(exchange);

        if (segments.length == 0 && method.equals("GET")) {
            listUsers(exchange);
        } else if (segments.length == 0 && method.equals("POST")) {
            createUser(exchange);
        } else if (segments.length == 1 && method.equals("PUT")) {
            setActive(exchange, segments[0]);
        } else {
            sendJson(exchange, 404, JsonUtil.fail("Not found"));
        }
    }

    private void listUsers(HttpExchange exchange) throws IOException {
        if (requireAdmin(exchange) == null) {
            return;
        }
        ControllerResult<List<User>> result = userController.listUsers();
        sendJson(exchange, result.isSuccess() ? 200 : 400,
                JsonUtil.fromResult(result, list -> JsonUtil.arrayOf(list, JsonUtil::userAdminJson)));
    }

    private void createUser(HttpExchange exchange) throws IOException {
        if (requireAdmin(exchange) == null) {
            return;
        }
        JSONObject body = readBody(exchange);
        ControllerResult<User> result = userController.createUser(
                body.optString("username", null), body.optString("password", null),
                body.optString("role", null));
        sendJson(exchange, result.isSuccess() ? 200 : 400, JsonUtil.fromResult(result, JsonUtil::userAdminJson));
    }

    private void setActive(HttpExchange exchange, String idText) throws IOException {
        User admin = requireAdmin(exchange);
        if (admin == null) {
            return;
        }
        int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, JsonUtil.fail("Invalid user id"));
            return;
        }
        JSONObject body = readBody(exchange);
        boolean active = body.optBoolean("active", true);

        if (admin.getId() == id && !active) {
            sendJson(exchange, 400, JsonUtil.fail("You cannot deactivate your own account."));
            return;
        }

        User existing;
        try {
            existing = userDAO.findById(id);
        } catch (SQLException e) {
            sendJson(exchange, 500, JsonUtil.fail("Database error: " + e.getMessage()));
            return;
        }
        if (existing == null) {
            sendJson(exchange, 404, JsonUtil.fail("User not found"));
            return;
        }

        ControllerResult<User> result = userController.setActive(existing, active);
        if (result.isSuccess() && !active) {
            TokenStore.invalidateForUser(id);
        }
        sendJson(exchange, result.isSuccess() ? 200 : 400, JsonUtil.fromResult(result, JsonUtil::userAdminJson));
    }
}
