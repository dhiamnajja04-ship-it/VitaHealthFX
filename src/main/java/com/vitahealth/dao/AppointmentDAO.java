package com.vitahealth.dao;

import com.vitahealth.entity.Appointment;
import com.vitahealth.entity.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public boolean createAppointment(Appointment appointment) {
        String sql = "INSERT INTO appointment (patient_id, doctor_id, date, reason, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointment.getPatientId());
            pstmt.setInt(2, appointment.getDoctorId());
            pstmt.setTimestamp(3, Timestamp.valueOf(appointment.getDate()));
            pstmt.setString(4, appointment.getReason());
            pstmt.setString(5, appointment.getStatus());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Appointment> getAppointmentsByPatient(int patientId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, u.first_name as doctor_first, u.last_name as doctor_last " +
                "FROM appointment a JOIN user u ON a.doctor_id = u.id " +
                "WHERE a.patient_id = ? ORDER BY a.date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Appointment app = extractAppointmentFromResultSet(rs);
                app.setDoctorName(rs.getString("doctor_first") + " " + rs.getString("doctor_last"));
                appointments.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    public List<Appointment> getAppointmentsByDoctor(int doctorId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, u.first_name as patient_first, u.last_name as patient_last " +
                "FROM appointment a JOIN user u ON a.patient_id = u.id " +
                "WHERE a.doctor_id = ? ORDER BY a.date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Appointment app = extractAppointmentFromResultSet(rs);
                app.setPatientName(rs.getString("patient_first") + " " + rs.getString("patient_last"));
                appointments.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, p.first_name as patient_first, p.last_name as patient_last, " +
                "d.first_name as doctor_first, d.last_name as doctor_last " +
                "FROM appointment a " +
                "JOIN user p ON a.patient_id = p.id " +
                "JOIN user d ON a.doctor_id = d.id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Appointment app = extractAppointmentFromResultSet(rs);
                app.setPatientName(rs.getString("patient_first") + " " + rs.getString("patient_last"));
                app.setDoctorName(rs.getString("doctor_first") + " " + rs.getString("doctor_last"));
                appointments.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    public boolean updateAppointmentStatus(int appointmentId, String status) {
        String sql = "UPDATE appointment SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, appointmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cancelAppointment(int appointmentId) {
        return updateAppointmentStatus(appointmentId, "CANCELLED");
    }

    public List<User> getAllDoctors() {
        List<User> doctors = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE role = 'DOCTOR'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User doctor = new User();
                doctor.setId(rs.getInt("id"));
                doctor.setFirstName(rs.getString("first_name"));
                doctor.setLastName(rs.getString("last_name"));
                doctors.add(doctor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctors;
    }

    private Appointment extractAppointmentFromResultSet(ResultSet rs) throws SQLException {
        Appointment app = new Appointment();
        app.setId(rs.getInt("id"));
        app.setPatientId(rs.getInt("patient_id"));
        app.setDoctorId(rs.getInt("doctor_id"));
        Timestamp ts = rs.getTimestamp("date");
        if (ts != null) app.setDate(ts.toLocalDateTime());
        app.setReason(rs.getString("reason"));
        app.setStatus(rs.getString("status"));
        return app;
    }
}