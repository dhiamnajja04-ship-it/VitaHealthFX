package tn.esprit.workshopjdbc.dao;

import com.vitahealth.config.DatabaseConnection;
import tn.esprit.workshopjdbc.Entities.ForumComment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ForumCommentDAO {
    public boolean create(ForumComment comment) {
        ForumSchemaInitializer.ensureSchema();
        String sql = "INSERT INTO forum_comments (post_id, author_id, content, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, comment.getPostId());
            pstmt.setInt(2, comment.getAuthorId());
            pstmt.setString(3, comment.getContent());
            pstmt.setString(4, comment.getStatus());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) comment.setId(rs.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ForumComment> findByPost(int postId) {
        ForumSchemaInitializer.ensureSchema();
        List<ForumComment> comments = new ArrayList<>();
        String sql = """
                SELECT c.*, u.first_name, u.last_name, u.role
                FROM forum_comments c
                JOIN `user` u ON c.author_id = u.id
                WHERE c.post_id = ? AND c.status <> 'DELETED'
                ORDER BY c.created_at ASC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                comments.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    private ForumComment mapResultSet(ResultSet rs) throws SQLException {
        ForumComment comment = new ForumComment();
        comment.setId(rs.getInt("id"));
        comment.setPostId(rs.getInt("post_id"));
        comment.setAuthorId(rs.getInt("author_id"));
        comment.setAuthorName(rs.getString("first_name") + " " + rs.getString("last_name"));
        comment.setAuthorRole(rs.getString("role"));
        comment.setContent(rs.getString("content"));
        comment.setStatus(rs.getString("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) comment.setCreatedAt(createdAt.toLocalDateTime());
        return comment;
    }
}
