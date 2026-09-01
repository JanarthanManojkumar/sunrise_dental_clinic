package controller;

import dao.AppointmentDAO;
import dao.BillDAO;
import java.sql.SQLException;
import java.util.List;
import model.Appointment;
import model.DailyAppointmentCount;
import model.DentistRevenue;

public class ReportController {

    private final AppointmentDAO appointmentDAO;
    private final BillDAO billDAO;

    public ReportController() {
        this(new AppointmentDAO(), new BillDAO());
    }

    public ReportController(AppointmentDAO appointmentDAO, BillDAO billDAO) {
        this.appointmentDAO = appointmentDAO;
        this.billDAO = billDAO;
    }

    public ControllerResult<List<DailyAppointmentCount>> appointmentsPerDay() {
        try {
            return ControllerResult.success(appointmentDAO.countPerDay());
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }

    public ControllerResult<List<DentistRevenue>> revenuePerDentist() {
        try {
            return ControllerResult.success(billDAO.revenuePerDentist());
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }

    public ControllerResult<List<Appointment>> upcomingAppointments() {
        try {
            return ControllerResult.success(appointmentDAO.findUpcoming());
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }
}
