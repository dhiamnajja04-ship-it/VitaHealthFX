package com.vitahealth.dao;

import com.vitahealth.entity.User;
import com.vitahealth.entity.Role;
import org.example.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public boolean ajouter(User user) throws SQLException {
        String sql = "INSERT INTO user (email, password, first_name, last_name, role, is_verified, " +
                "specialite, diplome, cin, poids, taille, glycemie, tension, maladie) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFirstName());
            pstmt.setString(4, user.getLastName());
            pstmt.setString(5, user.getRole());
            pstmt.setBoolean(6, user.isVerified());
            pstmt.setString(7, user.getSpecialite());
            pstmt.setString(8, user.getDiplome());
            pstmt.setString(9, user.getCin());
            pstmt.setObject(10, user.getPoids(), Types.DOUBLE);
            pstmt.setObject(11, user.getTaille(), Types.DOUBLE);
            pstmt.setObject(12, user.getGlycemie(), Types.DOUBLE);
            pstmt.setString(13, user.getTension());
            pstmt.setString(14, user.getMaladie());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        }
    }

    // ✅ Retourne un seul User, pas une List
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        }
        return null;
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user ORDER BY id";
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) users.add(mapResultSet(rs));
        }
        return users;
    }

    public List<User> findByRole(String role) throws SQLException {
        return rechercherParRole(role);
    }

    public List<User> getAllMedecins() throws SQLException {
        return findByRole(Role.MEDECIN);
    }

    public List<User> rechercherParNom(String nom) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE first_name LIKE ? OR last_name LIKE ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            String pattern = "%" + nom + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) users.add(mapResultSet(rs));
            }
        }
        return users;
    }

    public List<User> rechercherParEmail(String email) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE email LIKE ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, "%" + email + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) users.add(mapResultSet(rs));
            }
        }
        return users;
    }

    public List<User> rechercherParRole(String role) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE role = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, role);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) users.add(mapResultSet(rs));
            }
        }
        return users;
    }

    public void update(User user) throws SQLException {
        String sql = "UPDATE user SET email=?, password=?, first_name=?, last_name=?, role=?, " +
                "is_verified=?, specialite=?, diplome=?, cin=?, poids=?, taille=?, glycemie=?, tension=?, maladie=? " +
                "WHERE id=?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFirstName());
            pstmt.setString(4, user.getLastName());
            pstmt.setString(5, user.getRole());
            pstmt.setBoolean(6, user.isVerified());
            pstmt.setString(7, user.getSpecialite());
            pstmt.setString(8, user.getDiplome());
            pstmt.setString(9, user.getCin());
            pstmt.setObject(10, user.getPoids(), Types.DOUBLE);
            pstmt.setObject(11, user.getTaille(), Types.DOUBLE);
            pstmt.setObject(12, user.getGlycemie(), Types.DOUBLE);
            pstmt.setString(13, user.getTension());
            pstmt.setString(14, user.getMaladie());
            pstmt.setInt(15, user.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public long compterParRole(String role) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE role = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, role);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return 0;
    }

    public double moyennePoidsPatients() throws SQLException {
        String sql = "SELECT AVG(poids) FROM user WHERE role = ? AND poids IS NOT NULL";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, Role.PATIENT);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    private User mapResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setRole(rs.getString("role"));
        user.setVerified(rs.getBoolean("is_verified"));
        user.setSpecialite(rs.getString("specialite"));
        user.setDiplome(rs.getString("diplome"));
        user.setCin(rs.getString("cin"));
        user.setPoids((Double) rs.getObject("poids"));
        user.setTaille((Double) rs.getObject("taille"));
        user.setGlycemie((Double) rs.getObject("glycemie"));
        user.setTension(rs.getString("tension"));
        user.setMaladie(rs.getString("maladie"));
        return user;
    }
}