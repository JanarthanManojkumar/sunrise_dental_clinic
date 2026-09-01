package util;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public final class SessionTimeoutMonitor {

    private static final int POLL_INTERVAL_MS = 5000;

    private static Timer pollTimer;
    private static AWTEventListener activityListener;

    private SessionTimeoutMonitor() {
    }

    public static void start(Runnable onTimeout) {
        stop();

        activityListener = event -> SessionManager.getInstance().touch();
        Toolkit.getDefaultToolkit().addAWTEventListener(activityListener,
                AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);

        pollTimer = new Timer(POLL_INTERVAL_MS, e -> {
            if (SessionManager.getInstance().isExpired()) {
                stop();
                SwingUtilities.invokeLater(onTimeout);
            }
        });
        pollTimer.setRepeats(true);
        pollTimer.start();
    }

    public static void stop() {
        if (pollTimer != null) {
            pollTimer.stop();
            pollTimer = null;
        }
        if (activityListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(activityListener);
            activityListener = null;
        }
    }
}
