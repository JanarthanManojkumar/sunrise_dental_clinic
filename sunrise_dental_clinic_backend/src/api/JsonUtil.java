package api;

import controller.ControllerResult;
import java.util.List;
import java.util.function.Function;
import model.Appointment;
import model.Bill;
import model.DailyAppointmentCount;
import model.Dentist;
import model.DentistRevenue;
import model.Patient;
import model.Treatment;
import model.User;
import org.json.JSONArray;
import org.json.JSONObject;

/** Maps the existing model POJOs to/from org.json objects for the REST layer. */
public final class JsonUtil {

    private JsonUtil() {
    }

    public static JSONObject envelope(boolean success, String message, Object data) {
        JSONObject json = new JSONObject();
        json.put("success", success);
        json.put("message", message);
        json.put("data", data == null ? JSONObject.NULL : data);
        return json;
    }

    public static JSONObject ok(Object data) {
        return envelope(true, "OK", data);
    }

    public static JSONObject fail(String message) {
        return envelope(false, message, null);
    }

    /** Wraps a ControllerResult, converting its data with the given mapper. */
    public static <T> JSONObject fromResult(ControllerResult<T> result, Function<T, Object> mapper) {
        if (!result.isSuccess()) {
            return fail(result.getMessage());
        }
        Object data = result.getData();
        return envelope(true, result.getMessage(), data == null ? null : mapper.apply(result.getData()));
    }

    public static JSONObject userJson(User user) {
        JSONObject json = new JSONObject();
        json.put("id", user.getId());
        json.put("username", user.getUsername());
        json.put("role", user.getRole().name());
        return json;
    }

    public static JSONObject dentistJson(Dentist dentist) {
        JSONObject json = new JSONObject();
        json.put("id", dentist.getId());
        json.put("name", dentist.getName());
        json.put("specialization", dentist.getSpecialization());
        return json;
    }

    public static JSONObject treatmentJson(Treatment treatment) {
        JSONObject json = new JSONObject();
        json.put("id", treatment.getId());
        json.put("name", treatment.getName());
        json.put("fee", treatment.getFee());
        return json;
    }

    public static JSONObject patientJson(Patient patient) {
        JSONObject json = new JSONObject();
        json.put("id", patient.getId());
        json.put("name", patient.getName());
        json.put("address", patient.getAddress());
        json.put("contactNumber", patient.getContactNumber());
        json.put("email", patient.getEmail() == null ? JSONObject.NULL : patient.getEmail());
        return json;
    }

    public static JSONObject appointmentJson(Appointment appointment) {
        JSONObject json = new JSONObject();
        json.put("id", appointment.getId());
        json.put("appointmentNo", appointment.getAppointmentNo());
        json.put("patient", patientJson(appointment.getPatient()));
        json.put("dentist", dentistJson(appointment.getDentist()));
        json.put("treatment", treatmentJson(appointment.getTreatment()));
        json.put("appointmentDate", appointment.getAppointmentDate().toString());
        json.put("appointmentTime", appointment.getAppointmentTime().toString());
        json.put("status", appointment.getStatus().name());
        return json;
    }

    public static JSONObject dailyAppointmentCountJson(DailyAppointmentCount row) {
        JSONObject json = new JSONObject();
        json.put("date", row.date().toString());
        json.put("count", row.count());
        return json;
    }

    public static JSONObject dentistRevenueJson(DentistRevenue row) {
        JSONObject json = new JSONObject();
        json.put("dentistName", row.dentistName());
        json.put("revenue", row.revenue());
        return json;
    }

    public static <T> JSONArray arrayOf(List<T> items, Function<T, JSONObject> mapper) {
        JSONArray array = new JSONArray();
        for (T item : items) {
            array.put(mapper.apply(item));
        }
        return array;
    }
}
