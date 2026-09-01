package util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import model.User;

public final class SessionManager {

    public static final int DEFAULT_TIMEOUT_MINUTES = 15;

    private static final SessionManager INSTANCE = new SessionManager();

    private User currentUser;
    private String sessionId;
    private LocalDateTime loginTime;
    private LocalDateTime lastActivityTime;
    private int timeoutMinutes = DEFAULT_TIMEOUT_MINUTES;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public synchronized void startSession(User user) {
        this.currentUser = user;
        this.sessionId = UUID.randomUUID().toString();
        this.loginTime = LocalDateTime.now();
        this.lastActivityTime = this.loginTime;
    }

    public synchronized void endSession() {
        this.currentUser = null;
        this.sessionId = null;
        this.loginTime = null;
        this.lastActivityTime = null;
    }

    public synchronized boolean isActive() {
        return currentUser != null;
    }

    public synchronized void touch() {
        if (isActive()) {
            this.lastActivityTime = LocalDateTime.now();
        }
    }

    public synchronized boolean isExpired() {
        if (!isActive()) {
            return false;
        }
        return Duration.between(lastActivityTime, LocalDateTime.now()).toMinutes() >= timeoutMinutes;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized String getSessionId() {
        return sessionId;
    }

    public synchronized LocalDateTime getLoginTime() {
        return loginTime;
    }

    public synchronized LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }

    public synchronized int getTimeoutMinutes() {
        return timeoutMinutes;
    }

    public synchronized void setTimeoutMinutes(int timeoutMinutes) {
        this.timeoutMinutes = timeoutMinutes;
    }
}
