package com.vitahealth.dao;

import com.vitahealth.entity.User;
import com.vitahealth.entity.Role;
import com.vitahealth.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // ✅ AUTHENTICATE
    public User authenticate(String email, String password) {
        String sql = "SELECT * FROM user WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ✅ CREATE USER (pour AdminDashboardController)
    public boolean createUser(User user) {
        String sql = "INSERT INTO user (email, password, first_name, last_name, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFirstName());
            pstmt.setString(4, user.getLastName());
            pstmt.setString(5, user.getRole());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ UPDATE USER (pour AdminDashboardController)
    public boolean updateUser(User user) {
        String sql = "UPDATE user SET first_name=?, last_name=?, email=?, role=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getFirstName());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getRole());
            pstmt.setInt(5, user.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ DELETE USER (pour AdminDashboardController)
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM user WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean ajouter(User user) throws SQLException {
        String sql = "INSERT INTO user (email, password, first_name, last_name, role, is_verified, " +
                "specialite, diplome, cin, poids, taille, glycemie, tension, maladie) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) users.add(mapResultSet(rs));
        }
        return users;
    }

    public List<User> getAllUsers() throws SQLException {
        return findAll();
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
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public long compterParRole(String role) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE role = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return 0;
    }

    public double moyennePoidsPatients() throws SQLException {
        String sql = "SELECT AVG(poids) FROM user WHERE role = ? AND poids IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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