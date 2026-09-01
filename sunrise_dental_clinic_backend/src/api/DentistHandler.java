package api;

import com.sun.net.httpserver.HttpExchange;
import controller.ControllerResult;
import controller.DentistController;
import dao.DentistDAO;
import java.io.IOException;
import java.sql.SQLException;
import model.Dentist;
import model.User;
import org.json.JSONObject;

/** Handles /api/dentists and /api/dentists/{id}. */
public class DentistHandler extends BaseHandler {

    private final DentistController dentistController = new DentistController();
    private final DentistDAO dentistDAO = new DentistDAO();

    @Override
    protected void route(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String[] segments = subPath(exchange);

        if (segments.length == 0 && method.equals("GET")) {
            listDentists(exchange);
        } else if (segments.length == 0 && method.equals("POST")) {
            addDentist(exchange);
        } else if (segments.length == 1 && method.equals("PUT")) {
            updateDentist(exchange, segments[0]);
        } else {
            sendJson(exchange, 404, JsonUtil.fail("Not found"));
        }
    }

    private void listDentists(HttpExchange exchange) throws IOException {
        if (requireAuth(exchange) == null) {
            return;
        }
        ControllerResult<java.util.List<Dentist>> result = dentistController.listDentists();
        sendJson(exchange, result.isSuccess() ? 200 : 400,
                JsonUtil.fromResult(result, list -> JsonUtil.arrayOf(list, JsonUtil::dentistJson)));
    }

    private void addDentist(HttpExchange exchange) throws IOException {
        if (requireAdmin(exchange) == null) {
            return;
        }
        JSONObject body = readBody(exchange);
        ControllerResult<Dentist> result = dentistController.addDentist(
                body.optString("name", null), body.optString("specialization", null));
        sendJson(exchange, result.isSuccess() ? 200 : 400, JsonUtil.fromResult(result, JsonUtil::dentistJson));
    }

    private void updateDentist(HttpExchange exchange, String idText) throws IOException {
        User admin = requireAdmin(exchange);
        if (admin == null) {
            return;
        }
        int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, JsonUtil.fail("Invalid dentist id"));
            return;
        }
        Dentist existing;
        try {
            existing = dentistDAO.findById(id);
        } catch (SQLException e) {
            sendJson(exchange, 500, JsonUtil.fail("Database error: " + e.getMessage()));
            return;
        }
        if (existing == null) {
            sendJson(exchange, 404, JsonUtil.fail("Dentist not found"));
            return;
        }
        JSONObject body = readBody(exchange);
        ControllerResult<Dentist> result = dentistController.updateDentist(existing,
                body.optString("name", null), body.optString("specialization", null));
        sendJson(exchange, result.isSuccess() ? 200 : 400, JsonUtil.fromResult(result, JsonUtil::dentistJson));
    }
}
