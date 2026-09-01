package api;

import com.sun.net.httpserver.HttpExchange;
import controller.ControllerResult;
import controller.LoginController;
import java.io.IOException;
import model.User;
import org.json.JSONObject;

/** Handles POST /api/login and POST /api/logout. */
public class AuthHandler extends BaseHandler {

    private final LoginController loginController = new LoginController();

    @Override
    protected void route(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.equals("/api/login") && method.equals("POST")) {
            login(exchange);
        } else if (path.equals("/api/logout") && method.equals("POST")) {
            logout(exchange);
        } else {
            sendJson(exchange, 404, JsonUtil.fail("Not found"));
        }
    }

    private void login(HttpExchange exchange) throws IOException {
        JSONObject body = readBody(exchange);
        String username = body.optString("username", null);
        String password = body.optString("password", null);

        ControllerResult<User> result = loginController.login(username, password);
        if (!result.isSuccess()) {
            sendJson(exchange, 400, JsonUtil.fail(result.getMessage()));
            return;
        }
        User user = result.getData();
        String token = TokenStore.issue(user);
        JSONObject data = JsonUtil.userJson(user);
        data.put("token", token);
        sendJson(exchange, 200, JsonUtil.ok(data));
    }

    private void logout(HttpExchange exchange) throws IOException {
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7).trim() : null;
        TokenStore.invalidate(token);
        sendJson(exchange, 200, JsonUtil.ok(null));
    }
}
