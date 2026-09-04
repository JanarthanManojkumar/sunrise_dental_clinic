package api;

import com.sun.net.httpserver.HttpExchange;
import controller.AppointmentController;
import controller.ControllerResult;
import dao.DentistDAO;
import dao.TreatmentDAO;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Appointment;
import model.Dentist;
import model.Treatment;
import org.json.JSONObject;

/** Handles /api/appointments (list/filter), /upcoming, /{no}, /{no}/cancel. */
public class AppointmentHandler extends BaseHandler {

    private final AppointmentController appointmentController = new AppointmentController();
    private final DentistDAO dentistDAO = new DentistDAO();
    private final TreatmentDAO treatmentDAO = new TreatmentDAO();

    @Override
    protected void route(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String[] segments = subPath(exchange);

        if (segments.length == 0 && method.equals("POST")) {
            register(exchange);
        } else if (segments.length == 0 && method.equals("GET")) {
            list(exchange);
        } else if (segments.length == 1 && segments[0].equals("upcoming") && method.equals("GET")) {
            upcoming(exchange);
        } else if (segments.length == 1 && method.equals("GET")) {
            search(exchange, segments[0]);
        } else if (segments.length == 1 && method.equals("PUT")) {
            update(exchange, segments[0]);
        } else if (segments.length == 2 && segments[1].equals("cancel") && method.equals("POST")) {
            cancel(exchange, segments[0]);
        } else {
            sendJson(exchange, 404, JsonUtil.fail("Not found"));
        }
    }

    private void register(HttpExchange exchange) throws IOException {
        if (requireAuth(exchange) == null) {
            return;
        }
        JSONObject body = readBody(exchange);

        Dentist dentist = findDentist(body);
        Treatment treatment = findTreatment(body);
        LocalDate date = parseDate(body);
        LocalTime time = parseTime(body);
        if (date == INVALID_DATE_MARKER || time == INVALID_TIME_MARKER) {
            sendJson(exchange, 400, JsonUtil.fail(date == INVALID_DATE_MARKER
                    ? "Invalid date, use YYYY-MM-DD" : "Invalid time, use HH:mm"));
            return;
        }

        Integer existingPatientId = body.has("existingPatientId") && !body.isNull("existingPatientId")
                ? Integer.valueOf(body.optInt("existingPatientId")) : null;

        ControllerResult<Appointment> result = appointmentController.registerAppointment(
                body.optString("patientName", null), body.optString("address", null),
                body.optString("contactNumber", null), body.optString("email", null),
                dentist, treatment, date, time, existingPatientId);
        sendJson(exchange, result.isSuccess() ? 200 : 400, JsonUtil.fromResult(result, JsonUtil::appointmentJson));
    }

    private void search(HttpExchange exchange, String appointmentNo) throws IOException {
        if (requireAuth(exchange) == null) {
            return;
        }
        ControllerResult<Appointment> result = appointmentController.searchAppointment(appointmentNo);
        sendJson(exchange, result.isSuccess() ? 200 : 404, JsonUtil.fromResult(result, JsonUtil::appointmentJson));
    }

    private void update(HttpExchange exchange, String appointmentNo) throws IOException {
        if (requireAuth(exchange) == null) {
            return;
        }
        ControllerResult<Appointment> existingResult = appointmentController.searchAppointment(appointmentNo);
        if (!existingResult.isSuccess()) {
            sendJson(exchange, 404, JsonUtil.fail(existingResult.getMessage()));
            return;
        }

        JSONObject body = readBody(exchange);
        Dentist dentist = findDentist(body);
        Treatment treatment = findTreatment(body);
        LocalDate date = parseDate(body);
        LocalTime time = parseTime(body);
        if (date == INVALID_DATE_MARKER || time == INVALID_TIME_MARKER) {
            sendJson(exchange, 400, JsonUtil.fail(date == INVALID_DATE_MARKER
                    ? "Invalid date, use YYYY-MM-DD" : "Invalid time, use HH:mm"));
            return;
        }

        ControllerResult<Appointment> result = appointmentController.updateAppointment(
                existingResult.getData(), dentist, treatment, date, time);
        sendJson(exchange, result.isSuccess() ? 200 : 400, JsonUtil.fromResult(result, JsonUtil::appointmentJson));
    }

    private void cancel(HttpExchange exchange, String appointmentNo) throws IOException {
        if (requireAuth(exchange) == null) {
            return;
        }
        ControllerResult<Appointment> result = appointmentController.cancelAppointment(appointmentNo);
        sendJson(exchange, result.isSuccess() ? 200 : 400, JsonUtil.fromResult(result, JsonUtil::appointmentJson));
    }

    private void upcoming(HttpExchange exchange) throws IOException {
        if (requireAuth(exchange) == null) {
            return;
        }
        ControllerResult<List<Appointment>> result = appointmentController.listUpcoming();
        sendJson(exchange, result.isSuccess() ? 200 : 400,
                JsonUtil.fromResult(result, list -> JsonUtil.arrayOf(list, JsonUtil::appointmentJson)));
    }

    private void list(HttpExchange exchange) throws IOException {
        if (requireAuth(exchange) == null) {
            return;
        }
        Map<String, String> query = parseQuery(exchange);
        LocalDate date = parseOptionalDate(query.get("date"));
        LocalDate dateFrom = parseOptionalDate(query.get("dateFrom"));
        LocalDate dateTo = parseOptionalDate(query.get("dateTo"));
        Integer dentistId = null;
        try {
            dentistId = query.containsKey("dentistId") ? Integer.valueOf(query.get("dentistId")) : null;
        } catch (NumberFormatException e) {
            dentistId = null;
        }
        String patient = query.get("patient");

        ControllerResult<List<Appointment>> result =
                appointmentController.listAppointments(date, dateFrom, dateTo, dentistId, patient);
        sendJson(exchange, result.isSuccess() ? 200 : 400,
                JsonUtil.fromResult(result, list -> JsonUtil.arrayOf(list, JsonUtil::appointmentJson)));
    }

    private LocalDate parseOptionalDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Map<String, String> parseQuery(HttpExchange exchange) {
        Map<String, String> params = new HashMap<>();
        String raw = exchange.getRequestURI().getRawQuery();
        if (raw == null || raw.isEmpty()) {
            return params;
        }
        for (String pair : raw.split("&")) {
            int eq = pair.indexOf('=');
            String key = eq >= 0 ? pair.substring(0, eq) : pair;
            String value = eq >= 0 ? pair.substring(eq + 1) : "";
            try {
                key = URLDecoder.decode(key, StandardCharsets.UTF_8.name());
                value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                // UTF-8 is always supported
            }
            params.put(key, value);
        }
        return params;
    }

    private Dentist findDentist(JSONObject body) throws IOException {
        try {
            return dentistDAO.findById(body.optInt("dentistId", -1));
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    private Treatment findTreatment(JSONObject body) throws IOException {
        try {
            return treatmentDAO.findById(body.optInt("treatmentId", -1));
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    private static final LocalDate INVALID_DATE_MARKER = LocalDate.MIN;
    private static final LocalTime INVALID_TIME_MARKER = LocalTime.MAX;

    private LocalDate parseDate(JSONObject body) {
        try {
            return LocalDate.parse(body.optString("date", "").trim());
        } catch (DateTimeParseException e) {
            return INVALID_DATE_MARKER;
        }
    }

    private LocalTime parseTime(JSONObject body) {
        try {
            return LocalTime.parse(body.optString("time", "").trim());
        } catch (DateTimeParseException e) {
            return INVALID_TIME_MARKER;
        }
    }
}
