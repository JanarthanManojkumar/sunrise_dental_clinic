package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import model.Appointment;
import model.AppointmentStatus;
import model.DailyAppointmentCount;
import model.Dentist;
import model.Patient;
import model.Treatment;

public class AppointmentDAO {

    private static final String JOIN_SELECT =
            "SELECT a.id, a.appointment_no, a.appointment_date, a.appointment_time, a.status, "
            + "p.id AS patient_id, p.name AS patient_name, p.address AS patient_address, "
            + "p.contact_number AS patient_contact, p.email AS patient_email, "
            + "d.id AS dentist_id, d.name AS dentist_name, d.specialization AS dentist_specialization, "
            + "t.id AS treatment_id, t.name AS treatment_name, t.fee AS treatment_fee "
            + "FROM appointments a "
            + "JOIN patients p ON a.patient_id = p.id "
            + "JOIN dentists d ON a.dentist_id = d.id "
            + "JOIN treatments t ON a.treatment_id = t.id ";

    public int insert(Appointment appointment) throws SQLException {
        String sql = "INSERT INTO appointments "
                + "(appointment_no, patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, appointment.getAppointmentNo());
            pst.setInt(2, appointment.getPatient().getId());
            pst.setInt(3, appointment.getDentist().getId());
            pst.setInt(4, appointment.getTreatment().getId());
            pst.setObject(5, appointment.getAppointmentDate());
            pst.setObject(6, appointment.getAppointmentTime());
            pst.setString(7, appointment.getStatus().name());
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    appointment.setId(id);
                    return id;
                }
                throw new SQLException("Insert appointment failed, no generated key obtained.");
            }
        }
    }

    public Appointment findByAppointmentNo(String appointmentNo) throws SQLException {
        String sql = JOIN_SELECT + "WHERE a.appointment_no = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, appointmentNo);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Appointment> findUpcoming() throws SQLException {
        String sql = JOIN_SELECT
                + "WHERE a.appointment_date >= CURDATE() AND a.status = 'SCHEDULED' "
                + "ORDER BY a.appointment_date, a.appointment_time";
        Connection con = DBConnection.getInstance().getConnection();
        List<Appointment> appointments = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                appointments.add(mapRow(rs));
            }
        }
        return appointments;
    }

    public List<Appointment> findFiltered(LocalDate date, LocalDate dateFrom, LocalDate dateTo,
            Integer dentistId, String patientQuery) throws SQLException {
        StringBuilder sql = new StringBuilder(JOIN_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (date != null) {
            sql.append("AND a.appointment_date = ? ");
            params.add(date);
        }
        if (dateFrom != null) {
            sql.append("AND a.appointment_date >= ? ");
            params.add(dateFrom);
        }
        if (dateTo != null) {
            sql.append("AND a.appointment_date <= ? ");
            params.add(dateTo);
        }
        if (dentistId != null) {
            sql.append("AND d.id = ? ");
            params.add(dentistId);
        }
        if (patientQuery != null && !patientQuery.isBlank()) {
            sql.append("AND (p.name LIKE ? OR p.contact_number LIKE ?) ");
            String like = "%" + patientQuery.trim() + "%";
            params.add(like);
            params.add(like);
        }
        sql.append("ORDER BY a.appointment_date, a.appointment_time");

        Connection con = DBConnection.getInstance().getConnection();
        List<Appointment> appointments = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pst.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapRow(rs));
                }
            }
        }
        return appointments;
    }

    public List<DailyAppointmentCount> countPerDay() throws SQLException {
        String sql = "SELECT appointment_date, COUNT(*) AS total "
                + "FROM appointments GROUP BY appointment_date ORDER BY appointment_date";
        Connection con = DBConnection.getInstance().getConnection();
        List<DailyAppointmentCount> counts = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                counts.add(new DailyAppointmentCount(rs.getObject("appointment_date", LocalDate.class),
                        rs.getInt("total")));
            }
        }
        return counts;
    }

    public void update(Appointment appointment) throws SQLException {
        String sql = "UPDATE appointments SET dentist_id = ?, treatment_id = ?, "
                + "appointment_date = ?, appointment_time = ? WHERE id = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, appointment.getDentist().getId());
            pst.setInt(2, appointment.getTreatment().getId());
            pst.setObject(3, appointment.getAppointmentDate());
            pst.setObject(4, appointment.getAppointmentTime());
            pst.setInt(5, appointment.getId());
            pst.executeUpdate();
        }
    }

    public void updateStatus(int id, AppointmentStatus status) throws SQLException {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, status.name());
            pst.setInt(2, id);
            pst.executeUpdate();
        }
    }

    public void cancel(int id) throws SQLException {
        updateStatus(id, AppointmentStatus.CANCELLED);
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Patient patient = new Patient(rs.getInt("patient_id"), rs.getString("patient_name"),
                rs.getString("patient_address"), rs.getString("patient_contact"), rs.getString("patient_email"));
        Dentist dentist = new Dentist(rs.getInt("dentist_id"), rs.getString("dentist_name"),
                rs.getString("dentist_specialization"));
        Treatment treatment = new Treatment(rs.getInt("treatment_id"), rs.getString("treatment_name"),
                rs.getBigDecimal("treatment_fee"));

        return new Appointment.Builder()
                .id(rs.getInt("id"))
                .appointmentNo(rs.getString("appointment_no"))
                .patient(patient)
                .dentist(dentist)
                .treatment(treatment)
                .appointmentDate(rs.getObject("appointment_date", LocalDate.class))
                .appointmentTime(rs.getObject("appointment_time", LocalTime.class))
                .status(AppointmentStatus.valueOf(rs.getString("status")))
                .build();
    }
}
