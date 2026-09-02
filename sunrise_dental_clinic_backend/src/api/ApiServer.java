package api;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Standalone REST API entry point, separate from the Swing desktop app's
 * main class. Run with: java -cp "build\classes;lib\*" api.ApiServer
 */
public class ApiServer {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        server.createContext("/api/login", new AuthHandler());
        server.createContext("/api/logout", new AuthHandler());
        server.createContext("/api/dentists", new DentistHandler());
        server.createContext("/api/treatments", new TreatmentHandler());
        server.createContext("/api/appointments", new AppointmentHandler());
        server.createContext("/api/patients", new PatientHandler());
        server.createContext("/api/users", new UserHandler());
        server.createContext("/api/bills", new BillingHandler());
        server.createContext("/api/reports", new ReportHandler());

        server.start();
        System.out.println("Sunrise Dental Clinic API listening on http://localhost:" + PORT);
    }
}
