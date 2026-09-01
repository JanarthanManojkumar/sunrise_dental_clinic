package api;

import com.sun.net.httpserver.HttpExchange;
import controller.ControllerResult;
import controller.ReportController;
import java.io.IOException;

/** Handles /api/reports/appointments-per-day, /revenue-per-dentist, /upcoming. */
public class ReportHandler extends BaseHandler {

    private final ReportController reportController = new ReportController();

    @Override
    protected void route(HttpExchange exchange) throws IOException {
        if (requireAdmin(exchange) == null) {
            return;
        }
        String method = exchange.getRequestMethod();
        String[] segments = subPath(exchange);

        if (segments.length != 1 || !method.equals("GET")) {
            sendJson(exchange, 404, JsonUtil.fail("Not found"));
            return;
        }

        switch (segments[0]) {
            case "appointments-per-day" -> {
                ControllerResult<java.util.List<model.DailyAppointmentCount>> result =
                        reportController.appointmentsPerDay();
                sendJson(exchange, result.isSuccess() ? 200 : 400, JsonUtil.fromResult(result,
                        list -> JsonUtil.arrayOf(list, JsonUtil::dailyAppointmentCountJson)));
            }
            case "revenue-per-dentist" -> {
                ControllerResult<java.util.List<model.DentistRevenue>> result =
                        reportController.revenuePerDentist();
                sendJson(exchange, result.isSuccess() ? 200 : 400, JsonUtil.fromResult(result,
                        list -> JsonUtil.arrayOf(list, JsonUtil::dentistRevenueJson)));
            }
            case "upcoming" -> {
                ControllerResult<java.util.List<model.Appointment>> result =
                        reportController.upcomingAppointments();
                sendJson(exchange, result.isSuccess() ? 200 : 400, JsonUtil.fromResult(result,
                        list -> JsonUtil.arrayOf(list, JsonUtil::appointmentJson)));
            }
            default -> sendJson(exchange, 404, JsonUtil.fail("Not found"));
        }
    }
}
