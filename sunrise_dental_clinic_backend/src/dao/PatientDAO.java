package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Patient;

public class PatientDAO {

    public int insert(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (name, address, contact_number, email) VALUES (?, ?, ?, ?)";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, patient.getName());
            pst.setString(2, patient.getAddress());
            pst.setString(3, patient.getContactNumber());
            pst.setString(4, patient.getEmail());
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    patient.setId(id);
                    return id;
                }
                throw new SQLException("Insert patient failed, no generated key obtained.");
            }
        }
    }

    public List<Patient> search(String query) throws SQLException {
        String sql = "SELECT p.id, p.name, p.address, p.contact_number, p.email "
                + "FROM patients p "
                + "INNER JOIN (SELECT contact_number, MAX(id) AS max_id FROM patients GROUP BY contact_number) latest "
                + "  ON p.contact_number = latest.contact_number AND p.id = latest.max_id "
                + "WHERE p.name LIKE ? OR p.contact_number LIKE ? "
                + "ORDER BY p.name";
        String like = "%" + (query == null ? "" : query.trim()) + "%";
        Connection con = DBConnection.getInstance().getConnection();
        List<Patient> patients = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, like);
            pst.setString(2, like);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    patients.add(new Patient(rs.getInt("id"), rs.getString("name"),
                            rs.getString("address"), rs.getString("contact_number"), rs.getString("email")));
                }
            }
        }
        return patients;
    }

    public Patient findById(int id) throws SQLException {
        String sql = "SELECT id, name, address, contact_number, email FROM patients WHERE id = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Patient(rs.getInt("id"), rs.getString("name"),
                            rs.getString("address"), rs.getString("contact_number"), rs.getString("email"));
                }
                return null;
            }
        }
    }
}
