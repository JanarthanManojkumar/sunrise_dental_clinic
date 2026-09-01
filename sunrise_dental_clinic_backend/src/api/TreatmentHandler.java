package api;

import com.sun.net.httpserver.HttpExchange;
import controller.ControllerResult;
import controller.TreatmentController;
import dao.TreatmentDAO;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import model.Treatment;
import model.User;
import org.json.JSONObject;

/** Handles /api/treatments and /api/treatments/{id}. */
public class TreatmentHandler extends BaseHandler {

    private final TreatmentController treatmentController = new TreatmentController();
    private final TreatmentDAO treatmentDAO = new TreatmentDAO();

    @Override
    protected void route(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String[] segments = subPath(exchange);

        if (segments.length == 0 && method.equals("GET")) {
            listTreatments(exchange);
        } else if (segments.length == 0 && method.equals("POST")) {
            addTreatment(exchange);
        } else if (segments.length == 1 && method.equals("PUT")) {
            updateTreatment(exchange, segments[0]);
        } else {
            sendJson(exchange, 404, JsonUtil.fail("Not found"));
        }
    }

    private void listTreatments(HttpExchange exchange) throws IOException {
        if (requireAuth(exchange) == null) {
            return;
        }
        ControllerResult<java.util.List<Treatment>> result = treatmentController.listTreatments();
        sendJson(exchange, result.isSuccess() ? 200 : 400,
                JsonUtil.fromResult(result, list -> JsonUtil.arrayOf(list, JsonUtil::treatmentJson)));
    }

    private void addTreatment(HttpExchange exchange) throws IOException {
        if (requireAdmin(exchange) == null) {
            return;
        }
        JSONObject body = readBody(exchange);
        BigDecimal fee = parseFee(body);
        ControllerResult<Treatment> result = treatmentController.addTreatment(body.optString("name", null), fee);
        sendJson(exchange, result.isSuccess() ? 200 : 400, JsonUtil.fromResult(result, JsonUtil::treatmentJson));
    }

    private void updateTreatment(HttpExchange exchange, String idText) throws IOException {
        User admin = requireAdmin(exchange);
        if (admin == null) {
            return;
        }
        int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, JsonUtil.fail("Invalid treatment id"));
            return;
        }
        Treatment existing;
        try {
            existing = treatmentDAO.findById(id);
        } catch (SQLException e) {
            sendJson(exchange, 500, JsonUtil.fail("Database error: " + e.getMessage()));
            return;
        }
        if (existing == null) {
            sendJson(exchange, 404, JsonUtil.fail("Treatment not found"));
            return;
        }
        JSONObject body = readBody(exchange);
        BigDecimal fee = parseFee(body);
        ControllerResult<Treatment> result = treatmentController.updateTreatment(existing,
                body.optString("name", null), fee);
        sendJson(exchange, result.isSuccess() ? 200 : 400, JsonUtil.fromResult(result, JsonUtil::treatmentJson));
    }

    private BigDecimal parseFee(JSONObject body) {
        try {
            return new BigDecimal(body.get("fee").toString());
        } catch (Exception e) {
            return null;
        }
    }
}
