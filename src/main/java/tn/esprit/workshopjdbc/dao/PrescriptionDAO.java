package tn.esprit.workshopjdbc.dao;

import tn.esprit.workshopjdbc.Entities.Prescription;
import com.vitahealth.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {

    public boolean ajouter(int patientId, int medecinId, Prescription p) {
        String sql = "INSERT INTO prescriptions (patient_id, medecin_id, medication_list, instructions, duration) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, patientId);
            pstmt.setInt(2, medecinId);
            pstmt.setString(3, p.getMedicationList());
            pstmt.setString(4, p.getInstructions());
            pstmt.setString(5, p.getDuration());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Prescription> getByPatientId(int patientId) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions WHERE patient_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Prescription p = new Prescription();
                p.setId(rs.getInt("id"));
                p.setMedicationList(rs.getString("medication_list"));
                p.setInstructions(rs.getString("instructions"));
                p.setDuration(rs.getString("duration"));
                p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Prescription> getByMedecinId(int medecinId) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions WHERE medecin_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, medecinId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Prescription p = new Prescription();
                p.setId(rs.getInt("id"));
                p.setMedicationList(rs.getString("medication_list"));
                p.setInstructions(rs.getString("instructions"));
                p.setDuration(rs.getString("duration"));
                p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean modifier(Prescription p) {
        String sql = "UPDATE prescriptions SET medication_list=?, instructions=?, duration=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getMedicationList());
            pstmt.setString(2, p.getInstructions());
            pstmt.setString(3, p.getDuration());
            pstmt.setInt(4, p.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimer(int id) {
        String sql = "DELETE FROM prescriptions WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}