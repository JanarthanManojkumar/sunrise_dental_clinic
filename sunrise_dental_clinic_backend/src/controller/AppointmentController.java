package controller;

import dao.AppointmentDAO;
import dao.PatientDAO;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import model.Appointment;
import model.AppointmentStatus;
import model.Dentist;
import model.Patient;
import model.Treatment;
import util.AppointmentNumberGenerator;

public class AppointmentController {

    private static final String DUPLICATE_SLOT_KEY = "uq_dentist_slot";

    private final AppointmentDAO appointmentDAO;
    private final PatientDAO patientDAO;

    public AppointmentController() {
        this(new AppointmentDAO(), new PatientDAO());
    }

    public AppointmentController(AppointmentDAO appointmentDAO, PatientDAO patientDAO) {
        this.appointmentDAO = appointmentDAO;
        this.patientDAO = patientDAO;
    }

    public String validateAppointment(String patientName, String contactNumber, Dentist dentist,
            Treatment treatment, LocalDate date, LocalTime time) {
        if (patientName == null || patientName.trim().isEmpty()) {
            return "Patient name is required";
        }
        if (contactNumber == null || contactNumber.trim().isEmpty()) {
            return "Contact number is required";
        }
        if (dentist == null) {
            return "Dentist must be selected";
        }
        if (treatment == null) {
            return "Treatment must be selected";
        }
        if (date == null || date.isBefore(LocalDate.now())) {
            return "Appointment date cannot be in the past";
        }
        if (time == null) {
            return "Appointment time is required";
        }
        return "VALID";
    }

    public ControllerResult<Appointment> registerAppointment(String patientName, String address,
            String contactNumber, String email, Dentist dentist, Treatment treatment, LocalDate date,
            LocalTime time) {
        String validation = validateAppointment(patientName, contactNumber, dentist, treatment, date, time);
        if (!validation.equals("VALID")) {
            return ControllerResult.failure(validation);
        }
        try {
            Patient patient = new Patient();
            patient.setName(patientName.trim());
            patient.setAddress(address);
            patient.setContactNumber(contactNumber.trim());
            patient.setEmail(email == null || email.isBlank() ? null : email.trim());
            patientDAO.insert(patient);

            String appointmentNo = AppointmentNumberGenerator.generate(date);

            Appointment appointment = new Appointment.Builder()
                    .appointmentNo(appointmentNo)
                    .patient(patient)
                    .dentist(dentist)
                    .treatment(treatment)
                    .appointmentDate(date)
                    .appointmentTime(time)
                    .build();

            appointmentDAO.insert(appointment);
            return ControllerResult.success(appointment);
        } catch (SQLIntegrityConstraintViolationException e) {
            return ControllerResult.failure(duplicateSlotMessage(e));
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }

    public ControllerResult<Appointment> searchAppointment(String appointmentNo) {
        try {
            Appointment appointment = appointmentDAO.findByAppointmentNo(appointmentNo);
            if (appointment == null) {
                return ControllerResult.failure("No appointment found with number " + appointmentNo);
            }
            return ControllerResult.success(appointment);
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }

    public ControllerResult<Appointment> updateAppointment(Appointment appointment, Dentist newDentist,
            Treatment newTreatment, LocalDate newDate, LocalTime newTime) {
        String validation = validateAppointment(appointment.getPatient().getName(),
                appointment.getPatient().getContactNumber(), newDentist, newTreatment, newDate, newTime);
        if (!validation.equals("VALID")) {
            return ControllerResult.failure(validation);
        }
        try {
            appointment.setDentist(newDentist);
            appointment.setTreatment(newTreatment);
            appointment.setAppointmentDate(newDate);
            appointment.setAppointmentTime(newTime);
            appointmentDAO.update(appointment);
            return ControllerResult.success(appointment);
        } catch (SQLIntegrityConstraintViolationException e) {
            return ControllerResult.failure(duplicateSlotMessage(e));
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }

    public ControllerResult<Appointment> cancelAppointment(String appointmentNo) {
        try {
            Appointment appointment = appointmentDAO.findByAppointmentNo(appointmentNo);
            if (appointment == null) {
                return ControllerResult.failure("No appointment found with number " + appointmentNo);
            }
            appointmentDAO.cancel(appointment.getId());
            appointment.setStatus(AppointmentStatus.CANCELLED);
            return ControllerResult.success(appointment);
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }

    public ControllerResult<List<Appointment>> listUpcoming() {
        try {
            return ControllerResult.success(appointmentDAO.findUpcoming());
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }

    private String duplicateSlotMessage(SQLIntegrityConstraintViolationException e) {
        if (e.getMessage() != null && e.getMessage().contains(DUPLICATE_SLOT_KEY)) {
            return "This dentist already has an appointment at that date and time.";
        }
        return "Could not save appointment: " + e.getMessage();
    }
}
