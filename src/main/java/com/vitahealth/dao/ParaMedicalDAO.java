package com.vitahealth.dao;

import com.vitahealth.entity.ParaMedical;
import org.example.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParaMedicalDAO {

    public boolean ajouter(int userId, ParaMedical pm) {
        String sql = "INSERT INTO para_medical (user_id, poids, taille, glycemie, tension_systolique) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userId);
            pstmt.setDouble(2, pm.getPoids());
            pstmt.setDouble(3, pm.getTaille());
            pstmt.setDouble(4, pm.getGlycemie());
            pstmt.setString(5, pm.getTensionSystolique());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) pm.setId(rs.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ParaMedical> getByUserId(int userId) {
        List<ParaMedical> list = new ArrayList<>();
        String sql = "SELECT * FROM para_medical WHERE user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ParaMedical pm = new ParaMedical();
                pm.setId(rs.getInt("id"));
                pm.setPoids(rs.getDouble("poids"));
                pm.setTaille(rs.getDouble("taille"));
                pm.setGlycemie(rs.getDouble("glycemie"));
                pm.setTensionSystolique(rs.getString("tension_systolique"));
                pm.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(pm);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean modifier(ParaMedical pm) {
        String sql = "UPDATE para_medical SET poids=?, taille=?, glycemie=?, tension_systolique=? WHERE id=?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setDouble(1, pm.getPoids());
            pstmt.setDouble(2, pm.getTaille());
            pstmt.setDouble(3, pm.getGlycemie());
            pstmt.setString(4, pm.getTensionSystolique());
            pstmt.setInt(5, pm.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimer(int id) {
        String sql = "DELETE FROM para_medical WHERE id=?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}