package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
