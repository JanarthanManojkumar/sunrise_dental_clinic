package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Dentist;

public class DentistDAO {

    public List<Dentist> findAll() throws SQLException {
        String sql = "SELECT id, name, specialization FROM dentists ORDER BY name";
        Connection con = DBConnection.getInstance().getConnection();
        List<Dentist> dentists = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                dentists.add(mapRow(rs));
            }
        }
        return dentists;
    }

    public Dentist findById(int id) throws SQLException {
        String sql = "SELECT id, name, specialization FROM dentists WHERE id = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public int insert(Dentist dentist) throws SQLException {
        String sql = "INSERT INTO dentists (name, specialization) VALUES (?, ?)";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, dentist.getName());
            pst.setString(2, dentist.getSpecialization());
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    dentist.setId(id);
                    return id;
                }
                throw new SQLException("Insert dentist failed, no generated key obtained.");
            }
        }
    }

    public void update(Dentist dentist) throws SQLException {
        String sql = "UPDATE dentists SET name = ?, specialization = ? WHERE id = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, dentist.getName());
            pst.setString(2, dentist.getSpecialization());
            pst.setInt(3, dentist.getId());
            pst.executeUpdate();
        }
    }

    private Dentist mapRow(ResultSet rs) throws SQLException {
        return new Dentist(rs.getInt("id"), rs.getString("name"), rs.getString("specialization"));
    }
}
