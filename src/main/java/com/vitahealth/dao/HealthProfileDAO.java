package com.vitahealth.dao;

import com.vitahealth.entity.HealthProfile;
import java.sql.*;

public class HealthProfileDAO {

    public HealthProfile getHealthProfileByUserId(int userId) {
        String sql = "SELECT * FROM health_profile WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                HealthProfile profile = new HealthProfile();
                profile.setId(rs.getInt("id"));
                profile.setUserId(rs.getInt("user_id"));
                profile.setHeight(rs.getDouble("height"));
                profile.setWeight(rs.getDouble("weight"));
                profile.setBloodType(rs.getString("blood_type"));
                profile.setAllergies(rs.getString("allergies"));
                profile.setChronicDiseases(rs.getString("chronic_diseases"));
                profile.setEmergencyContact(rs.getString("emergency_contact"));
                profile.setEmergencyPhone(rs.getString("emergency_phone"));
                return profile;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean saveOrUpdateHealthProfile(HealthProfile profile) {
        HealthProfile existing = getHealthProfileByUserId(profile.getUserId());
        if (existing == null) {
            return createHealthProfile(profile);
        } else {
            profile.setId(existing.getId());
            return updateHealthProfile(profile);
        }
    }

    private boolean createHealthProfile(HealthProfile profile) {
        String sql = "INSERT INTO health_profile (user_id, height, weight, blood_type, allergies, chronic_diseases, emergency_contact, emergency_phone) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, profile.getUserId());
            pstmt.setDouble(2, profile.getHeight());
            pstmt.setDouble(3, profile.getWeight());
            pstmt.setString(4, profile.getBloodType());
            pstmt.setString(5, profile.getAllergies());
            pstmt.setString(6, profile.getChronicDiseases());
            pstmt.setString(7, profile.getEmergencyContact());
            pstmt.setString(8, profile.getEmergencyPhone());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateHealthProfile(HealthProfile profile) {
        String sql = "UPDATE health_profile SET height=?, weight=?, blood_type=?, allergies=?, chronic_diseases=?, emergency_contact=?, emergency_phone=? WHERE user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, profile.getHeight());
            pstmt.setDouble(2, profile.getWeight());
            pstmt.setString(3, profile.getBloodType());
            pstmt.setString(4, profile.getAllergies());
            pstmt.setString(5, profile.getChronicDiseases());
            pstmt.setString(6, profile.getEmergencyContact());
            pstmt.setString(7, profile.getEmergencyPhone());
            pstmt.setInt(8, profile.getUserId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}