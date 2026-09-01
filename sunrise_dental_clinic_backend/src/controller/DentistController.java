package controller;

import dao.DentistDAO;
import java.sql.SQLException;
import java.util.List;
import model.Dentist;

public class DentistController {

    private final DentistDAO dentistDAO;

    public DentistController() {
        this(new DentistDAO());
    }

    public DentistController(DentistDAO dentistDAO) {
        this.dentistDAO = dentistDAO;
    }

    public String validateDentist(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Dentist name is required";
        }
        return "VALID";
    }

    public ControllerResult<List<Dentist>> listDentists() {
        try {
            return ControllerResult.success(dentistDAO.findAll());
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }

    public ControllerResult<Dentist> addDentist(String name, String specialization) {
        String validation = validateDentist(name);
        if (!validation.equals("VALID")) {
            return ControllerResult.failure(validation);
        }
        try {
            Dentist dentist = new Dentist();
            dentist.setName(name.trim());
            dentist.setSpecialization(specialization);
            dentistDAO.insert(dentist);
            return ControllerResult.success(dentist);
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }

    public ControllerResult<Dentist> updateDentist(Dentist dentist, String name, String specialization) {
        String validation = validateDentist(name);
        if (!validation.equals("VALID")) {
            return ControllerResult.failure(validation);
        }
        try {
            dentist.setName(name.trim());
            dentist.setSpecialization(specialization);
            dentistDAO.update(dentist);
            return ControllerResult.success(dentist);
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }
}
