package tn.esprit.workshopjdbc.Services;

import tn.esprit.workshopjdbc.Entities.Participation;
import tn.esprit.workshopjdbc.Entities.Event;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Utils.DbConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationService {
    private Connection cnx = DbConnection.getInstance().getCnx();

    // =========================================================================
    // 1. CREATE / REGISTER METHODS
    // =========================================================================

    /**
     * Used by EventRegistrationController (The Form)
     */
    public void add(Participation p) {
        String sql = "INSERT INTO participation (event_id, user_id, participant_name, phone, emergency_contact, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, p.getEvent().getId());
            if (p.getUser() != null) ps.setInt(2, p.getUser().getId());
            else ps.setNull(2, java.sql.Types.INTEGER);
            ps.setString(3, p.getParticipantName());
            ps.setString(4, p.getPhone());
            ps.setString(5, p.getEmergencyContact());
            ps.setString(6, p.getNote());
            ps.setTimestamp(7, Timestamp.valueOf(p.getCreatedAt()));
            ps.executeUpdate();
            System.out.println("✅ Participation added successfully.");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Used by PatientDashboard (Quick Toggle Join)
     */
    public void participate(int eventId, int userId) {
        Participation p = new Participation();
        Event e = new Event(); e.setId(eventId);
        User u = new User(); u.setId(userId);
        p.setEvent(e);
        p.setUser(u);
        p.setParticipantName("Patient " + userId);
        p.setPhone("00000000");
        p.setCreatedAt(java.time.LocalDateTime.now());
        add(p);
    }

    // =========================================================================
    // 2. READ / FETCH METHODS
    // =========================================================================

    /**
     * Checks if a user is already registered.
     */
    public boolean isUserParticipating(int eventId, int userId) {
        String sql = "SELECT COUNT(*) FROM participation WHERE event_id = ? AND user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Alias for EventRegistrationController validation
     */
    public boolean exists(int eventId, int userId) {
        return isUserParticipating(eventId, userId);
    }

    /**
     * Used by Admin to see all participations
     */
    public List<Participation> findAll() {
        List<Participation> list = new ArrayList<>();
        String sql = "SELECT p.*, e.title as event_title, e.date as event_date FROM participation p JOIN event e ON p.event_id = e.id";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToParticipation(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Used by ParticipationController (Admin side) to filter by event
     */
    public List<Participation> findByEvent(int eventId) {
        List<Participation> list = new ArrayList<>();
        String sql = "SELECT p.*, e.title as event_title, e.date as event_date FROM participation p " +
                "JOIN event e ON p.event_id = e.id WHERE p.event_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToParticipation(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // =========================================================================
    // 3. UPDATE METHODS
    // =========================================================================

    /**
     * Full update for editing participation details
     */
    public void update(Participation p) {
        String sql = "UPDATE participation SET participant_name = ?, phone = ?, emergency_contact = ?, note = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getParticipantName());
            ps.setString(2, p.getPhone());
            ps.setString(3, p.getEmergencyContact());
            ps.setString(4, p.getNote());
            ps.setInt(5, p.getId());
            ps.executeUpdate();
            System.out.println("✅ Participation updated.");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // =========================================================================
    // 4. DELETE / CANCEL METHODS
    // =========================================================================

    /**
     * Used by ParticipationController (Admin side) to delete by ID
     */
    public void delete(int id) {
        String sql = "DELETE FROM participation WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("🗑️ Participation deleted by ID: " + id);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /**
     * Used by PatientDashboard (Quick Toggle Leave)
     */
    public void cancelParticipation(int eventId, int userId) {
        String sql = "DELETE FROM participation WHERE event_id = ? AND user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            System.out.println("🗑️ Participation cancelled for user " + userId);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // =========================================================================
    // 5. HELPER MAPPING
    // =========================================================================

    private Participation mapResultSetToParticipation(ResultSet rs) throws SQLException {
        Participation p = new Participation();
        p.setId(rs.getInt("id"));
        p.setParticipantName(rs.getString("participant_name"));
        p.setPhone(rs.getString("phone"));
        p.setEmergencyContact(rs.getString("emergency_contact"));
        p.setNote(rs.getString("note"));
        p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        User u = new User(); u.setId(rs.getInt("user_id"));
        p.setUser(u);

        Event e = new Event(); e.setId(rs.getInt("event_id"));
        e.setTitle(rs.getString("event_title"));
        // Check if event_date exists in the join to avoid errors
        try {
            e.setDate(rs.getTimestamp("event_date").toLocalDateTime());
        } catch (Exception ex) { /* column might not be in all queries */ }
        p.setEvent(e);
        return p;
    }
    /**
     * Used by MyParticipationsController to show a specific user's registrations
     */
    public List<Participation> findByUser(int userId) {
        List<Participation> list = new ArrayList<>();
        // Note: We use 'event' as the table name based on your findAll() query
        String sql = "SELECT p.*, e.title as event_title, e.date as event_date " +
                "FROM participation p " +
                "JOIN event e ON p.event_id = e.id " +
                "WHERE p.user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToParticipation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}