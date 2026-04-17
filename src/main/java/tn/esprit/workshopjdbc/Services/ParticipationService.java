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

    /**
     * CREATE: Registers a user for an event using form data.
     */
    public void add(Participation p) {
        String sql = "INSERT INTO participation (event_id, user_id, participant_name, phone, emergency_contact, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, p.getEvent().getId());

            if (p.getUser() != null) {
                ps.setInt(2, p.getUser().getId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }

            ps.setString(3, p.getParticipantName());
            ps.setString(4, p.getPhone());
            ps.setString(5, p.getEmergencyContact());
            ps.setString(6, p.getNote());
            ps.setTimestamp(7, Timestamp.valueOf(p.getCreatedAt()));

            ps.executeUpdate();
            System.out.println("✅ Participation added successfully via Form.");
        } catch (SQLException e) {
            System.err.println("❌ Error adding participation: " + e.getMessage());
        }
    }

    /**
     * NEW HELPER: Checks if a user is already registered for a specific event.
     * Useful for the form validation before calling add().
     */
    public boolean exists(int eventId, int userId) {
        String sql = "SELECT COUNT(*) FROM participation WHERE event_id = ? AND user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * READ (ALL): Used by UserDashboard to show joined events.
     */
    public List<Participation> findAll() {
        List<Participation> list = new ArrayList<>();
        String sql = "SELECT p.*, e.title as event_title, e.date as event_date " +
                "FROM participation p " +
                "JOIN event e ON p.event_id = e.id";

        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToParticipation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * READ (BY EVENT): Used by Admin to see participants for a specific event.
     */
    public List<Participation> findByEvent(int eventId) {
        List<Participation> list = new ArrayList<>();
        String sql = "SELECT p.*, e.title as event_title, e.date as event_date " +
                "FROM participation p " +
                "JOIN event e ON p.event_id = e.id " +
                "WHERE p.event_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToParticipation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * UPDATE: Allows users to change contact info or notes from the Dashboard.
     */
    public void update(Participation p) {
        String sql = "UPDATE participation SET phone = ?, emergency_contact = ?, note = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getPhone());
            ps.setString(2, p.getEmergencyContact());
            ps.setString(3, p.getNote());
            ps.setInt(4, p.getId());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Participation info updated in DB.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * DELETE: Cancels a registration.
     */
    public void delete(int id) {
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM participation WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("🗑️ Participation record deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * MAPPING HELPER: Reconstructs objects from SQL result sets.
     */
    private Participation mapResultSetToParticipation(ResultSet rs) throws SQLException {
        Participation p = new Participation();
        p.setId(rs.getInt("id"));
        p.setParticipantName(rs.getString("participant_name"));
        p.setPhone(rs.getString("phone"));
        p.setEmergencyContact(rs.getString("emergency_contact"));
        p.setNote(rs.getString("note"));
        p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        User user = new User();
        user.setId(rs.getInt("user_id"));
        p.setUser(user);

        Event event = new Event();
        event.setId(rs.getInt("event_id"));
        event.setTitle(rs.getString("event_title"));
        event.setDate(rs.getTimestamp("event_date").toLocalDateTime());
        p.setEvent(event);

        return p;
    }
}