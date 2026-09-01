package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Treatment;

public class TreatmentDAO {

    public List<Treatment> findAll() throws SQLException {
        String sql = "SELECT id, name, fee FROM treatments ORDER BY name";
        Connection con = DBConnection.getInstance().getConnection();
        List<Treatment> treatments = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                treatments.add(mapRow(rs));
            }
        }
        return treatments;
    }

    public Treatment findById(int id) throws SQLException {
        String sql = "SELECT id, name, fee FROM treatments WHERE id = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public Treatment findByName(String name) throws SQLException {
        String sql = "SELECT id, name, fee FROM treatments WHERE name = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, name);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public int insert(Treatment treatment) throws SQLException {
        String sql = "INSERT INTO treatments (name, fee) VALUES (?, ?)";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, treatment.getName());
            pst.setBigDecimal(2, treatment.getFee());
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    treatment.setId(id);
                    return id;
                }
                throw new SQLException("Insert treatment failed, no generated key obtained.");
            }
        }
    }

    public void update(Treatment treatment) throws SQLException {
        String sql = "UPDATE treatments SET name = ?, fee = ? WHERE id = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, treatment.getName());
            pst.setBigDecimal(2, treatment.getFee());
            pst.setInt(3, treatment.getId());
            pst.executeUpdate();
        }
    }

    private Treatment mapRow(ResultSet rs) throws SQLException {
        return new Treatment(rs.getInt("id"), rs.getString("name"), rs.getBigDecimal("fee"));
    }
}
