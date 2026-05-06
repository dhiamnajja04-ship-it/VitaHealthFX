package tn.esprit.workshopjdbc.dao;

import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Entities.Role;
import com.vitahealth.config.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // ==================== AUTHENTIFICATION ====================
    
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

    // ==================== CRUD POUR ADMIN DASHBOARD ====================
    
    public boolean createUser(User user) {
        String sql = "INSERT INTO user (email, password, first_name, last_name, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
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

    public boolean updateUser(User user) {
        String sql = "UPDATE user SET first_name=?, last_name=?, email=?, role=?, updated_at=NOW() WHERE id=?";
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

    // ==================== AJOUTER UTILISATEUR COMPLET ====================
    
    public boolean ajouter(User user) throws SQLException {
        String sql = "INSERT INTO user (email, password, first_name, last_name, role, is_verified, " +
                "specialite, diplome, cin, poids, taille, glycemie, tension, maladie, phone, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

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
            pstmt.setString(15, user.getPhone());

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

    // ==================== RECHERCHE ====================
    
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
        String sql = "SELECT * FROM user ORDER BY id DESC";
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
        return findByRole("DOCTOR");
    }
    
    public List<User> getAllPatients() throws SQLException {
        return findByRole("PATIENT");
    }
    
    public List<User> getAllAdmins() throws SQLException {
        return findByRole("ADMIN");
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
    
    public List<User> rechercherParPhone(String phone) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE phone LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + phone + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) users.add(mapResultSet(rs));
            }
        }
        return users;
    }
    
    public List<User> rechercherParCin(String cin) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE cin LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + cin + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) users.add(mapResultSet(rs));
            }
        }
        return users;
    }

    // ==================== MISES À JOUR ====================
    
    public void update(User user) throws SQLException {
        String sql = "UPDATE user SET email=?, password=?, first_name=?, last_name=?, role=?, " +
                "is_verified=?, specialite=?, diplome=?, cin=?, poids=?, taille=?, glycemie=?, tension=?, maladie=?, phone=?, updated_at=NOW() " +
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
            pstmt.setString(15, user.getPhone());
            pstmt.setInt(16, user.getId());
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

    // ==================== GESTION DES RÔLES ====================
    
    public boolean updateUserRole(int userId, String newRole) throws SQLException {
        String sql = "UPDATE user SET role = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean updateVerificationStatus(int userId, boolean verified) throws SQLException {
        String sql = "UPDATE user SET is_verified = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, verified);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // ==================== STATISTIQUES ====================
    
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
            pstmt.setString(1, "PATIENT");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }
    
    public double moyenneTaillePatients() throws SQLException {
        String sql = "SELECT AVG(taille) FROM user WHERE role = ? AND taille IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "PATIENT");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }
    
    public double moyenneGlycemiePatients() throws SQLException {
        String sql = "SELECT AVG(glycemie) FROM user WHERE role = ? AND glycemie IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "PATIENT");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    // ==================== UTILITAIRES ====================
    
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    public long countAllUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM user";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }
    
    public long countVerifiedUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE is_verified = true";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }
    
    public long countUnverifiedUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE is_verified = false";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }
    
    public List<User> getRecentUsers(int limit) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user ORDER BY id DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSet(rs));
                }
            }
        }
        return users;
    }

    // ==================== MAPPER (AVEC createdAt ET updatedAt) ====================
    
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
        user.setPhone(rs.getString("phone"));
        
        // Ajout pour createdAt et updatedAt
        try {
            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                user.setCreatedAt(createdAt.toLocalDateTime());
            }
        } catch (SQLException e) {
            // Colonne n'existe pas encore
        }
        
        try {
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) {
                user.setUpdatedAt(updatedAt.toLocalDateTime());
            }
        } catch (SQLException e) {
            // Colonne n'existe pas encore
        }
        
        return user;
    }
}