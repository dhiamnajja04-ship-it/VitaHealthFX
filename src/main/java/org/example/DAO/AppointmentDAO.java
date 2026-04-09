package org.example.DAO;

import org.example.entity.Appointment;
import org.example.config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public void ajouter(Appointment rdv) throws SQLException {
        // Utilise les vrais noms de colonnes : doctor_id, date, reason
        String sql = "INSERT INTO appointment (patient_id, doctor_id, date, reason, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, rdv.getPatientId());
            pstmt.setInt(2, rdv.getDoctorId());           // ← doctorId
            pstmt.setTimestamp(3, Timestamp.valueOf(rdv.getDate())); // ← date
            pstmt.setString(4, rdv.getReason());          // ← reason
            pstmt.setString(5, "en_attente");             // statut par défaut
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    rdv.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<Appointment> getRdvByPatient(int patientId) throws SQLException {
        List<Appointment> rdvs = new ArrayList<>();
        // Note : la colonne primaire s'appelle "Id" (avec majuscule)
        String sql = "SELECT a.Id, a.patient_id, a.doctor_id, a.date, a.reason, a.status, u.first_name, u.last_name " +
                "FROM appointment a JOIN user u ON a.doctor_id = u.id " +
                "WHERE a.patient_id = ? ORDER BY a.date DESC";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Appointment rdv = new Appointment();
                rdv.setId(rs.getInt("Id"));
                rdv.setPatientId(rs.getInt("patient_id"));
                rdv.setDoctorId(rs.getInt("doctor_id"));
                rdv.setDate(rs.getTimestamp("date").toLocalDateTime());
                rdv.setReason(rs.getString("reason"));
                rdv.setStatus(rs.getString("status"));
                rdv.setDoctorNom(rs.getString("first_name") + " " + rs.getString("last_name"));
                rdvs.add(rdv);
            }
        }
        return rdvs;
    }

    public List<Appointment> getRdvByMedecin(int medecinId) throws SQLException {
        List<Appointment> rdvs = new ArrayList<>();
        // Récupère les rendez-vous d'un médecin avec les infos du patient
        String sql = "SELECT a.Id, a.patient_id, a.doctor_id, a.date, a.reason, a.status, u.first_name, u.last_name " +
                "FROM appointment a JOIN user u ON a.patient_id = u.id " +
                "WHERE a.doctor_id = ? ORDER BY a.date DESC";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, medecinId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Appointment rdv = new Appointment();
                rdv.setId(rs.getInt("Id"));
                rdv.setPatientId(rs.getInt("patient_id"));
                rdv.setDoctorId(rs.getInt("doctor_id"));
                rdv.setDate(rs.getTimestamp("date").toLocalDateTime());
                rdv.setReason(rs.getString("reason"));
                rdv.setStatus(rs.getString("status"));
                rdv.setDoctorNom(rs.getString("first_name") + " " + rs.getString("last_name"));
                rdvs.add(rdv);
            }
        }
        return rdvs;
    }

    public void updateStatus(int rdvId, String status) throws SQLException {
        // La colonne primaire s'appelle "Id"
        String sql = "UPDATE appointment SET status = ? WHERE Id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, rdvId);
            pstmt.executeUpdate();
        }
    }
}