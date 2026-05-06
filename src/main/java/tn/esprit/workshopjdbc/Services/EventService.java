package tn.esprit.workshopjdbc.Services;

import tn.esprit.workshopjdbc.Entities.Event;
import tn.esprit.workshopjdbc.Interfaces.IEvent;
import tn.esprit.workshopjdbc.Utils.DbConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventService implements IEvent {

    private Connection cnx = DbConnection.getInstance().getCnx();

    // NEW: Check if an event with same title and date exists
    public boolean exists(String title, LocalDateTime date) {
        String sql = "SELECT count(*) FROM `event` WHERE title = ? AND date = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setTimestamp(2, Timestamp.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error checking existence: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void add(Event event) {
        // Using backticks `event` to avoid SQL reserved word conflicts
        String sql = "INSERT INTO `event` (title, description, date, latitude, longitude) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(event.getDate()));
            ps.setFloat(4, event.getLatitude());
            ps.setFloat(5, event.getLongitude());
            ps.executeUpdate();
            System.out.println("✅ Event added!");
        } catch (SQLException e) {
            System.err.println("❌ Error add: " + e.getMessage());
        }
    }

    @Override
    public void update(Event event) {
        String sql = "UPDATE `event` SET title=?, description=?, date=?, latitude=?, longitude=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(event.getDate()));
            ps.setFloat(4, event.getLatitude());
            ps.setFloat(5, event.getLongitude());
            ps.setInt(6, event.getId());
            ps.executeUpdate();
            System.out.println("✅ Event updated!");
        } catch (SQLException e) {
            System.err.println("❌ Error update: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM `event` WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Event deleted!");
        } catch (SQLException e) {
            System.err.println("❌ Error delete: " + e.getMessage());
        }
    }

    @Override
    public Event findById(int id) {
        String sql = "SELECT * FROM `event` WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToEvent(rs);
        } catch (SQLException e) {
            System.err.println("❌ Error findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Event> findAll() {
        return getListFromQuery("SELECT * FROM `event` ORDER BY date ASC", null);
    }

    @Override
    public List<Event> searchByTitleOrDescription(String query) {
        String sql = "SELECT * FROM `event` WHERE title LIKE ? OR description LIKE ?";
        return getListFromQuery(sql, "%" + query + "%");
    }

    public List<Event> findEventsWithParticipants() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT DISTINCT e.* FROM `event` e INNER JOIN participation p ON e.id = p.event_id";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToEvent(rs));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    private List<Event> getListFromQuery(String sql, Object param) {
        List<Event> events = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (param != null) {
                if (param instanceof String) {
                    ps.setString(1, (String) param);
                    ps.setString(2, (String) param);
                } else if (param instanceof Timestamp) {
                    ps.setTimestamp(1, (Timestamp) param);
                }
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) events.add(mapResultSetToEvent(rs));
        } catch (SQLException e) { System.err.println("❌ Query Error: " + e.getMessage()); }
        return events;
    }

    private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setId(rs.getInt("id"));
        e.setTitle(rs.getString("title"));
        e.setDescription(rs.getString("description"));
        e.setDate(rs.getTimestamp("date").toLocalDateTime());
        e.setLatitude(rs.getFloat("latitude"));
        e.setLongitude(rs.getFloat("longitude"));
        return e;
    }

    // Explicit implementations for Interface requirements
    @Override public List<Event> findUpcoming() { return getListFromQuery("SELECT * FROM `event` WHERE date >= ? ORDER BY date ASC", Timestamp.valueOf(LocalDateTime.now())); }
    @Override public List<Event> findPast() { return getListFromQuery("SELECT * FROM `event` WHERE date < ? ORDER BY date DESC", Timestamp.valueOf(LocalDateTime.now())); }
}