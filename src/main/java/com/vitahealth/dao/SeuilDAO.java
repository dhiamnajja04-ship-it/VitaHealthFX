package com.vitahealth.dao;

import com.vitahealth.config.DatabaseConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SeuilDAO {
    public Map<String, Map<String, Double>> getSeuils() {
        Map<String, Map<String, Double>> seuils = new HashMap<>();
        String sql = "SELECT param_type, min_normal, max_normal FROM seuils";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String type = rs.getString("param_type");
                double min = rs.getDouble("min_normal");
                double max = rs.getDouble("max_normal");
                Map<String, Double> couple = new HashMap<>();
                couple.put("min", min);
                couple.put("max", max);
                seuils.put(type, couple);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return seuils;
    }

    public boolean updateSeuil(String paramType, double minNormal, double maxNormal) {
        String sql = "UPDATE seuils SET min_normal = ?, max_normal = ? WHERE param_type = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, minNormal);
            pstmt.setDouble(2, maxNormal);
            pstmt.setString(3, paramType);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}