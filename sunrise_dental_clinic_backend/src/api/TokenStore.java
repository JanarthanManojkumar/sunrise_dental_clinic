package api;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import model.User;

/**
 * Per-token session store for the REST API. util.SessionManager is a single
 * process-wide singleton (fine for the one Swing process) which can't hold
 * more than one logged-in user at a time, so the web API needs its own
 * token-keyed sessions to let multiple browsers be logged in concurrently.
 */
public final class TokenStore {

    private static final int TIMEOUT_MINUTES = 15;

    private static final ConcurrentHashMap<String, Session> SESSIONS = new ConcurrentHashMap<>();

    private TokenStore() {
    }

    private static final class Session {
        final User user;
        LocalDateTime lastActivity;

        Session(User user) {
            this.user = user;
            this.lastActivity = LocalDateTime.now();
        }
    }

    public static String issue(User user) {
        String token = UUID.randomUUID().toString();
        SESSIONS.put(token, new Session(user));
        return token;
    }

    public static User validate(String token) {
        if (token == null) {
            return null;
        }
        Session session = SESSIONS.get(token);
        if (session == null) {
            return null;
        }
        if (Duration.between(session.lastActivity, LocalDateTime.now()).toMinutes() >= TIMEOUT_MINUTES) {
            SESSIONS.remove(token);
            return null;
        }
        session.lastActivity = LocalDateTime.now();
        return session.user;
    }

    public static void invalidate(String token) {
        if (token != null) {
            SESSIONS.remove(token);
        }
    }

    /** Drops every live session for a user, e.g. right after their account is deactivated. */
    public static void invalidateForUser(int userId) {
        SESSIONS.entrySet().removeIf(entry -> entry.getValue().user.getId() == userId);
    }
}
