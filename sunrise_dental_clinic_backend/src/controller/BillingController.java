package controller;

import dao.AppointmentDAO;
import dao.BillDAO;
import dao.TreatmentDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import model.Appointment;
import model.AppointmentStatus;
import model.Bill;
import model.Treatment;
import util.BrevoEmailService;
import util.EmailException;
import util.EmailService;
import util.ReceiptFactory;

public class BillingController {

    private static final String CONSULTATION_TREATMENT_NAME = "Consultation";

    private final AppointmentDAO appointmentDAO;
    private final TreatmentDAO treatmentDAO;
    private final BillDAO billDAO;
    private final EmailService emailService;

    public BillingController() {
        this(new AppointmentDAO(), new TreatmentDAO(), new BillDAO(), new BrevoEmailService());
    }

    public BillingController(AppointmentDAO appointmentDAO, TreatmentDAO treatmentDAO, BillDAO billDAO) {
        this(appointmentDAO, treatmentDAO, billDAO, new BrevoEmailService());
    }

    public BillingController(AppointmentDAO appointmentDAO, TreatmentDAO treatmentDAO, BillDAO billDAO,
            EmailService emailService) {
        this.appointmentDAO = appointmentDAO;
        this.treatmentDAO = treatmentDAO;
        this.billDAO = billDAO;
        this.emailService = emailService;
    }

    public BigDecimal calculateTotal(BigDecimal consultationFee, BigDecimal treatmentFee) {
        return consultationFee.add(treatmentFee);
    }

    public ControllerResult<String> generateBill(String appointmentNo) {
        try {
            Appointment appointment = appointmentDAO.findByAppointmentNo(appointmentNo);
            if (appointment == null) {
                return ControllerResult.failure("No appointment found with number " + appointmentNo);
            }
            if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                return ControllerResult.failure("Cannot bill a cancelled appointment.");
            }

            Bill existing = billDAO.findByAppointmentId(appointment.getId());
            if (existing != null) {
                return ControllerResult.success(ReceiptFactory.createReceipt(appointment, existing));
            }

            Treatment consultation = treatmentDAO.findByName(CONSULTATION_TREATMENT_NAME);
            if (consultation == null) {
                return ControllerResult.failure("Consultation fee is not configured in the price list.");
            }

            BigDecimal consultationFee = consultation.getFee();
            BigDecimal treatmentFee = appointment.getTreatment().getFee();
            BigDecimal total = calculateTotal(consultationFee, treatmentFee);

            Bill bill = new Bill();
            bill.setAppointmentId(appointment.getId());
            bill.setConsultationFee(consultationFee);
            bill.setTreatmentFee(treatmentFee);
            bill.setTotal(total);
            bill.setIssuedAt(LocalDateTime.now());
            billDAO.insert(bill);

            return ControllerResult.success(ReceiptFactory.createReceipt(appointment, bill));
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }

    public ControllerResult<String> getBill(String appointmentNo) {
        try {
            Appointment appointment = appointmentDAO.findByAppointmentNo(appointmentNo);
            if (appointment == null) {
                return ControllerResult.failure("No appointment found with number " + appointmentNo);
            }

            Bill bill = billDAO.findByAppointmentId(appointment.getId());
            if (bill == null) {
                return ControllerResult.failure("Bill has not been generated yet.");
            }

            return ControllerResult.success(ReceiptFactory.createReceipt(appointment, bill));
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }

    public ControllerResult<String> emailBill(String appointmentNo) {
        try {
            Appointment appointment = appointmentDAO.findByAppointmentNo(appointmentNo);
            if (appointment == null) {
                return ControllerResult.failure("No appointment found with number " + appointmentNo);
            }

            String email = appointment.getPatient().getEmail();
            if (email == null || email.isBlank()) {
                return ControllerResult.failure("This patient does not have an email address on file.");
            }

            Bill bill = billDAO.findByAppointmentId(appointment.getId());
            if (bill == null) {
                return ControllerResult.failure("Bill has not been generated yet.");
            }

            String receipt = ReceiptFactory.createReceipt(appointment, bill);
            String subject = "Your bill from Sunrise Dental Clinic - " + appointment.getAppointmentNo();
            emailService.sendBillEmail(email, appointment.getPatient().getName(), subject, receipt);

            return ControllerResult.success("Bill emailed to " + email);
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        } catch (EmailException e) {
            return ControllerResult.failure("Email error: " + e.getMessage());
        }
    }
}
