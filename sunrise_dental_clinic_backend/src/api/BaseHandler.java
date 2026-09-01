package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import model.Role;
import model.User;
import org.json.JSONObject;

/** Shared plumbing (CORS, auth, JSON body/response) for every REST handler. */
abstract class BaseHandler implements HttpHandler {

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {
            route(exchange);
        } catch (Exception e) {
            sendJson(exchange, 500, JsonUtil.fail("Server error: " + e.getMessage()));
        } finally {
            exchange.close();
        }
    }

    protected abstract void route(HttpExchange exchange) throws IOException;

    /** Path segments after the handler's context prefix, e.g. "/api/dentists/5" -> ["5"]. */
    protected String[] subPath(HttpExchange exchange) {
        String contextPath = exchange.getHttpContext().getPath();
        String full = exchange.getRequestURI().getPath();
        String rest = full.substring(contextPath.length());
        while (rest.startsWith("/")) {
            rest = rest.substring(1);
        }
        if (rest.isEmpty()) {
            return new String[0];
        }
        return rest.split("/");
    }

    protected JSONObject readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            in.transferTo(buffer);
            String text = buffer.toString(StandardCharsets.UTF_8).trim();
            return text.isEmpty() ? new JSONObject() : new JSONObject(text);
        }
    }

    protected void sendJson(HttpExchange exchange, int status, JSONObject body) throws IOException {
        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    /** Returns the authenticated user, or sends 401 and returns null. */
    protected User requireAuth(HttpExchange exchange) throws IOException {
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7).trim() : null;
        User user = TokenStore.validate(token);
        if (user == null) {
            sendJson(exchange, 401, JsonUtil.fail("Not authenticated. Please log in again."));
            return null;
        }
        return user;
    }

    /** Returns the authenticated admin user, or sends 401/403 and returns null. */
    protected User requireAdmin(HttpExchange exchange) throws IOException {
        User user = requireAuth(exchange);
        if (user == null) {
            return null;
        }
        if (user.getRole() != Role.ADMIN) {
            sendJson(exchange, 403, JsonUtil.fail("Admin privileges required."));
            return null;
        }
        return user;
    }
}
