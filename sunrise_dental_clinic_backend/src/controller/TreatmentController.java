package controller;

import dao.TreatmentDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import model.Treatment;

public class TreatmentController {

    private final TreatmentDAO treatmentDAO;

    public TreatmentController() {
        this(new TreatmentDAO());
    }

    public TreatmentController(TreatmentDAO treatmentDAO) {
        this.treatmentDAO = treatmentDAO;
    }

    public String validateTreatment(String name, BigDecimal fee) {
        if (name == null || name.trim().isEmpty()) {
            return "Treatment name is required";
        }
        if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
            return "Fee must be a positive amount";
        }
        return "VALID";
    }

    public ControllerResult<List<Treatment>> listTreatments() {
        try {
            return ControllerResult.success(treatmentDAO.findAll());
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }

    public ControllerResult<Treatment> addTreatment(String name, BigDecimal fee) {
        String validation = validateTreatment(name, fee);
        if (!validation.equals("VALID")) {
            return ControllerResult.failure(validation);
        }
        try {
            Treatment treatment = new Treatment();
            treatment.setName(name.trim());
            treatment.setFee(fee);
            treatmentDAO.insert(treatment);
            return ControllerResult.success(treatment);
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }

    public ControllerResult<Treatment> updateTreatment(Treatment treatment, String name, BigDecimal fee) {
        String validation = validateTreatment(name, fee);
        if (!validation.equals("VALID")) {
            return ControllerResult.failure(validation);
        }
        try {
            treatment.setName(name.trim());
            treatment.setFee(fee);
            treatmentDAO.update(treatment);
            return ControllerResult.success(treatment);
        } catch (SQLException e) {
            return ControllerResult.failure("Database error: " + e.getMessage());
        }
    }
}
