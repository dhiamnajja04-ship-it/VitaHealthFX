package tn.esprit.workshopjdbc.dao;

import com.vitahealth.config.DatabaseConnection;
import tn.esprit.workshopjdbc.Entities.Story;
import tn.esprit.workshopjdbc.Entities.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StoryDAO {
    
    public boolean create(Story story) {
        ensureSchema();
        
        // Set timestamps if not already set
        if (story.getCreatedAt() == null) {
            story.setCreatedAt(LocalDateTime.now());
        }
        if (story.getExpiresAt() == null) {
            story.setExpiresAt(LocalDateTime.now().plusHours(24));
        }
        
        String sql = "INSERT INTO stories (user_id, image_url, caption, created_at, expires_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, story.getUserId());
            pstmt.setString(2, story.getImageUrl());
            pstmt.setString(3, story.getCaption());
            pstmt.setTimestamp(4, Timestamp.valueOf(story.getCreatedAt()));
            pstmt.setTimestamp(5, Timestamp.valueOf(story.getExpiresAt()));
            
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    story.setId(rs.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Story> findActiveStories() {
        ensureSchema();
        List<Story> stories = new ArrayList<>();
        String sql = """
                SELECT s.*, u.first_name, u.last_name, u.role
                FROM stories s
                JOIN `user` u ON s.user_id = u.id
                WHERE s.expires_at > NOW()
                ORDER BY s.created_at DESC
                LIMIT 20
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Story story = mapResultSet(rs);
                stories.add(story);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stories;
    }

    public List<Story> findStoriesByUser(int userId) {
        ensureSchema();
        List<Story> stories = new ArrayList<>();
        String sql = """
                SELECT s.*, u.first_name, u.last_name, u.role
                FROM stories s
                JOIN `user` u ON s.user_id = u.id
                WHERE s.user_id = ? AND s.expires_at > NOW()
                ORDER BY s.created_at DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Story story = mapResultSet(rs);
                    stories.add(story);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stories;
    }

    public boolean incrementViews(int storyId) {
        ensureSchema();
        String sql = "UPDATE stories SET views = views + 1 WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, storyId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int storyId) {
        ensureSchema();
        String sql = "DELETE FROM stories WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, storyId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Story mapResultSet(ResultSet rs) throws SQLException {
        Story story = new Story();
        story.setId(rs.getInt("id"));
        story.setUserId(rs.getInt("user_id"));
        story.setImageUrl(rs.getString("image_url"));
        story.setCaption(rs.getString("caption"));
        story.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        story.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
        story.setViews(rs.getInt("views"));
        
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        story.setUserName(firstName + " " + lastName);
        story.setUserRole(rs.getString("role"));
        
        return story;
    }

    private void ensureSchema() {
        String createTable = """
                CREATE TABLE IF NOT EXISTS stories (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    image_url VARCHAR(255) NOT NULL,
                    caption VARCHAR(500),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    expires_at DATETIME NOT NULL,
                    views INT DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
                    INDEX (expires_at),
                    INDEX (user_id),
                    INDEX (created_at)
                )
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
