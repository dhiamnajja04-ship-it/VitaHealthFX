package tn.esprit.workshopjdbc.dao;

import com.vitahealth.config.DatabaseConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SeuilDAO {
    public Map<String, Map<String, Double>> getSeuils() {
        Map<String, Map<String, Double>> seuils = new HashMap<>();
        String sql = "SELECT param_type, min_normal, max_normal FROM seuils";
        try (Connection conn = DatabaseConnection.getConnection()) {
            ensureTable(conn);
            try (PreparedStatement stmt = conn.prepareStatement(sql);
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
            seuils.putAll(defaultSeuils());
        }
        if (seuils.isEmpty()) seuils.putAll(defaultSeuils());
        return seuils;
    }

    public boolean updateSeuil(String paramType, double minNormal, double maxNormal) {
        String sql = "UPDATE seuils SET min_normal = ?, max_normal = ? WHERE param_type = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            ensureTable(conn);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDouble(1, minNormal);
                pstmt.setDouble(2, maxNormal);
                pstmt.setString(3, paramType);
                if (pstmt.executeUpdate() > 0) return true;
            }
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO seuils (param_type, min_normal, max_normal) VALUES (?, ?, ?)")) {
                insert.setString(1, paramType);
                insert.setDouble(2, minNormal);
                insert.setDouble(3, maxNormal);
                return insert.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private void ensureTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS seuils (
                        param_type VARCHAR(50) PRIMARY KEY,
                        min_normal DOUBLE NOT NULL,
                        max_normal DOUBLE NOT NULL,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    )
                    """);
        }
        for (Map.Entry<String, Map<String, Double>> entry : defaultSeuils().entrySet()) {
            try (PreparedStatement stmt = conn.prepareStatement("""
                    INSERT INTO seuils (param_type, min_normal, max_normal)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE param_type = param_type
                    """)) {
                stmt.setString(1, entry.getKey());
                stmt.setDouble(2, entry.getValue().get("min"));
                stmt.setDouble(3, entry.getValue().get("max"));
                stmt.executeUpdate();
            }
        }
    }

    private Map<String, Map<String, Double>> defaultSeuils() {
        Map<String, Map<String, Double>> defaults = new HashMap<>();
        defaults.put("glycemie", pair(0.70, 1.10));
        defaults.put("tension", pair(90.0, 140.0));
        defaults.put("imc", pair(18.5, 24.9));
        return defaults;
    }

    private Map<String, Double> pair(double min, double max) {
        Map<String, Double> values = new HashMap<>();
        values.put("min", min);
        values.put("max", max);
        return values;
    }
}
