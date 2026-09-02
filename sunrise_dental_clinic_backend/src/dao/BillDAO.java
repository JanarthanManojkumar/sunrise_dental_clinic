package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.Bill;
import model.DentistRevenue;

public class BillDAO {

    public int insert(Bill bill) throws SQLException {
        String sql = "INSERT INTO bills (appointment_id, consultation_fee, treatment_fee, total) "
                + "VALUES (?, ?, ?, ?)";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, bill.getAppointmentId());
            pst.setBigDecimal(2, bill.getConsultationFee());
            pst.setBigDecimal(3, bill.getTreatmentFee());
            pst.setBigDecimal(4, bill.getTotal());
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    bill.setId(id);
                    return id;
                }
                throw new SQLException("Insert bill failed, no generated key obtained.");
            }
        }
    }

    public Bill findByAppointmentId(int appointmentId) throws SQLException {
        String sql = "SELECT id, appointment_id, consultation_fee, treatment_fee, total, issued_at "
                + "FROM bills WHERE appointment_id = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, appointmentId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Timestamp issuedAt = rs.getTimestamp("issued_at");
                    return new Bill(rs.getInt("id"), rs.getInt("appointment_id"),
                            rs.getBigDecimal("consultation_fee"), rs.getBigDecimal("treatment_fee"),
                            rs.getBigDecimal("total"), issuedAt.toLocalDateTime());
                }
                return null;
            }
        }
    }

    public List<DentistRevenue> revenuePerDentist() throws SQLException {
        String sql = "SELECT d.name AS dentist_name, SUM(b.total) AS revenue "
                + "FROM bills b "
                + "JOIN appointments a ON b.appointment_id = a.id "
                + "JOIN dentists d ON a.dentist_id = d.id "
                + "WHERE a.status <> 'CANCELLED' "
                + "GROUP BY d.name ORDER BY d.name";
        Connection con = DBConnection.getInstance().getConnection();
        List<DentistRevenue> revenues = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                revenues.add(new DentistRevenue(rs.getString("dentist_name"), rs.getBigDecimal("revenue")));
            }
        }
        return revenues;
    }
}
