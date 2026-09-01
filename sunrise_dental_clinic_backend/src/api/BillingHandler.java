package api;

import com.sun.net.httpserver.HttpExchange;
import controller.BillingController;
import controller.ControllerResult;
import java.io.IOException;

/** Handles /api/bills/{appointmentNo}/generate and /api/bills/{appointmentNo}/email. */
public class BillingHandler extends BaseHandler {

    private final BillingController billingController = new BillingController();

    @Override
    protected void route(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String[] segments = subPath(exchange);

        if (segments.length == 2 && segments[1].equals("generate") && method.equals("POST")) {
            generate(exchange, segments[0]);
        } else if (segments.length == 2 && segments[1].equals("email") && method.equals("POST")) {
            email(exchange, segments[0]);
        } else {
            sendJson(exchange, 404, JsonUtil.fail("Not found"));
        }
    }

    private void generate(HttpExchange exchange, String appointmentNo) throws IOException {
        if (requireAuth(exchange) == null) {
            return;
        }
        ControllerResult<String> result = billingController.generateBill(appointmentNo);
        sendJson(exchange, result.isSuccess() ? 200 : 400, JsonUtil.fromResult(result, (String text) -> text));
    }

    private void email(HttpExchange exchange, String appointmentNo) throws IOException {
        if (requireAuth(exchange) == null) {
            return;
        }
        ControllerResult<String> result = billingController.emailBill(appointmentNo);
        sendJson(exchange, result.isSuccess() ? 200 : 400, JsonUtil.fromResult(result, (String text) -> text));
    }
}
