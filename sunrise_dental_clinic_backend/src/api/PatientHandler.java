package api;

import com.sun.net.httpserver.HttpExchange;
import dao.AppointmentDAO;
import dao.PatientDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import model.Appointment;
import model.Patient;

/** Handles /api/patients (search) and /api/patients/{id} (detail + history). */
public class PatientHandler extends BaseHandler {

    private final PatientDAO patientDAO = new PatientDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    protected void route(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String[] segments = subPath(exchange);

        if (segments.length == 0 && method.equals("GET")) {
            search(exchange);
        } else if (segments.length == 1 && method.equals("GET")) {
            detail(exchange, segments[0]);
        } else {
            sendJson(exchange, 404, JsonUtil.fail("Not found"));
        }
    }

    private void search(HttpExchange exchange) throws IOException {
        if (requireAuth(exchange) == null) {
            return;
        }
        String query = queryParam(exchange, "q");
        try {
            List<Patient> patients = patientDAO.search(query);
            sendJson(exchange, 200, JsonUtil.ok(JsonUtil.arrayOf(patients, JsonUtil::patientJson)));
        } catch (SQLException e) {
            sendJson(exchange, 500, JsonUtil.fail("Database error: " + e.getMessage()));
        }
    }

    private void detail(HttpExchange exchange, String idText) throws IOException {
        if (requireAuth(exchange) == null) {
            return;
        }
        int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, JsonUtil.fail("Invalid patient id"));
            return;
        }
        try {
            Patient patient = patientDAO.findById(id);
            if (patient == null) {
                sendJson(exchange, 404, JsonUtil.fail("Patient not found"));
                return;
            }
            List<Appointment> history = appointmentDAO.findByContactNumber(patient.getContactNumber());
            sendJson(exchange, 200, JsonUtil.ok(JsonUtil.patientHistoryJson(patient, history)));
        } catch (SQLException e) {
            sendJson(exchange, 500, JsonUtil.fail("Database error: " + e.getMessage()));
        }
    }

    private String queryParam(HttpExchange exchange, String name) {
        String raw = exchange.getRequestURI().getRawQuery();
        if (raw == null) {
            return "";
        }
        for (String pair : raw.split("&")) {
            int eq = pair.indexOf('=');
            String key = eq >= 0 ? pair.substring(0, eq) : pair;
            if (key.equals(name)) {
                try {
                    return java.net.URLDecoder.decode(eq >= 0 ? pair.substring(eq + 1) : "", "UTF-8");
                } catch (java.io.UnsupportedEncodingException e) {
                    return "";
                }
            }
        }
        return "";
    }
}
