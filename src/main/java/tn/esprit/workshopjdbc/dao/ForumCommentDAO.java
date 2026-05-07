package tn.esprit.workshopjdbc.dao;

import com.vitahealth.config.DatabaseConnection;
import tn.esprit.workshopjdbc.Entities.ForumComment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ForumCommentDAO {
    public boolean create(ForumComment comment) {
        ForumSchemaInitializer.ensureSchema();
        String sql = "INSERT INTO forum_comments (post_id, parent_comment_id, author_id, content, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, comment.getPostId());
            if (comment.getParentCommentId() > 0) {
                pstmt.setInt(2, comment.getParentCommentId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setInt(3, comment.getAuthorId());
            pstmt.setString(4, comment.getContent());
            pstmt.setString(5, comment.getStatus());
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

    public List<ForumComment> findReplies(int commentId) {
        ForumSchemaInitializer.ensureSchema();
        List<ForumComment> replies = new ArrayList<>();
        String sql = """
                SELECT c.*, u.first_name, u.last_name, u.role
                FROM forum_comments c
                JOIN `user` u ON c.author_id = u.id
                WHERE c.parent_comment_id = ? AND c.status <> 'DELETED'
                ORDER BY c.created_at ASC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                replies.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return replies;
    }

    public boolean toggleLikeComment(int commentId, int userId) {
        ForumSchemaInitializer.ensureSchema();
        String checkSql = "SELECT COUNT(*) FROM forum_comment_likes WHERE comment_id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if already liked
            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setInt(1, commentId);
                checkPstmt.setInt(2, userId);
                ResultSet rs = checkPstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // Unlike
                    String deleteSql = "DELETE FROM forum_comment_likes WHERE comment_id = ? AND user_id = ?";
                    try (PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {
                        deletePstmt.setInt(1, commentId);
                        deletePstmt.setInt(2, userId);
                        deletePstmt.executeUpdate();
                    }
                    decrementLikeCount(commentId);
                    return false;
                }
            }

            // Like
            String insertSql = "INSERT INTO forum_comment_likes (comment_id, user_id) VALUES (?, ?)";
            try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                insertPstmt.setInt(1, commentId);
                insertPstmt.setInt(2, userId);
                insertPstmt.executeUpdate();
            }
            incrementLikeCount(commentId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void incrementLikeCount(int commentId) {
        ForumSchemaInitializer.ensureSchema();
        String sql = "UPDATE forum_comments SET like_count = like_count + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void decrementLikeCount(int commentId) {
        ForumSchemaInitializer.ensureSchema();
        String sql = "UPDATE forum_comments SET like_count = GREATEST(like_count - 1, 0) WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean update(ForumComment comment) {
        ForumSchemaInitializer.ensureSchema();
        String sql = "UPDATE forum_comments SET content = ?, updated_at = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, comment.getContent());
            pstmt.setInt(2, comment.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int commentId) {
        ForumSchemaInitializer.ensureSchema();
        String sql = "UPDATE forum_comments SET status = 'DELETED' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ForumComment findById(int commentId) {
        ForumSchemaInitializer.ensureSchema();
        String sql = """
                SELECT c.*, u.first_name, u.last_name, u.role
                FROM forum_comments c
                JOIN `user` u ON c.author_id = u.id
                WHERE c.id = ? AND c.status <> 'DELETED'
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ForumComment mapResultSet(ResultSet rs) throws SQLException {
        ForumComment comment = new ForumComment();
        comment.setId(rs.getInt("id"));
        comment.setPostId(rs.getInt("post_id"));
        comment.setParentCommentId(rs.getInt("parent_comment_id"));
        comment.setAuthorId(rs.getInt("author_id"));
        comment.setAuthorName(rs.getString("first_name") + " " + rs.getString("last_name"));
        comment.setAuthorRole(rs.getString("role"));
        comment.setContent(rs.getString("content"));
        comment.setStatus(rs.getString("status"));
        comment.setLikeCount(rs.getInt("like_count"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) comment.setCreatedAt(createdAt.toLocalDateTime());
        
        // Handle updated_at safely - it might not exist in older schemas
        try {
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) comment.setUpdatedAt(updatedAt.toLocalDateTime());
        } catch (SQLException e) {
            // Column doesn't exist, skip it
        }
        return comment;
    }
}
