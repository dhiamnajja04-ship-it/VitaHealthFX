package tn.esprit.workshopjdbc.dao;

import com.vitahealth.config.DatabaseConnection;
import tn.esprit.workshopjdbc.Entities.ForumPost;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ForumPostDAO {
    public boolean create(ForumPost post) {
        ForumSchemaInitializer.ensureSchema();
        String sql = "INSERT INTO forum_posts (category_id, author_id, title, content, language, status, image_url, video_url, tag) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, post.getCategoryId());
            pstmt.setInt(2, post.getAuthorId());
            pstmt.setString(3, post.getTitle());
            pstmt.setString(4, post.getContent());
            pstmt.setString(5, post.getLanguage());
            pstmt.setString(6, post.getStatus());
            pstmt.setString(7, post.getImageUrl());
            pstmt.setString(8, post.getVideoUrl());
            pstmt.setString(9, post.getTag());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) post.setId(rs.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ForumPost> search(String keyword, Integer categoryId, boolean includePending) {
        ForumSchemaInitializer.ensureSchema();
        List<ForumPost> posts = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT p.*, c.name AS category_name, u.first_name, u.last_name, u.role,
                       (SELECT COUNT(*) FROM forum_comments fc WHERE fc.post_id = p.id AND fc.status <> 'DELETED') AS calculated_comment_count,
                       (SELECT COUNT(*) FROM forum_reports fr WHERE fr.post_id = p.id AND fr.status = 'OPEN') AS report_count,
                       (SELECT COUNT(*) FROM forum_likes fl WHERE fl.post_id = p.id) AS calculated_like_count,
                       p.share_count
                FROM forum_posts p
                JOIN forum_categories c ON p.category_id = c.id
                JOIN `user` u ON p.author_id = u.id
                WHERE p.status <> 'DELETED'
                """);

        if (!includePending) {
            sql.append(" AND p.status = 'PUBLISHED'");
        }
        if (categoryId != null) {
            sql.append(" AND p.category_id = ?");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(p.title) LIKE ? OR LOWER(p.content) LIKE ?)");
        }
        sql.append(" ORDER BY p.created_at DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int index = 1;
            if (categoryId != null) {
                pstmt.setInt(index++, categoryId);
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                pstmt.setString(index++, pattern);
                pstmt.setString(index, pattern);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                posts.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public boolean incrementUsefulCount(int postId) {
        ForumSchemaInitializer.ensureSchema();
        String sql = "UPDATE forum_posts SET useful_count = useful_count + 1 WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean incrementLikeCount(int postId) {
        ForumSchemaInitializer.ensureSchema();
        String sql = "UPDATE forum_posts SET like_count = like_count + 1 WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean decrementLikeCount(int postId) {
        ForumSchemaInitializer.ensureSchema();
        String sql = "UPDATE forum_posts SET like_count = GREATEST(like_count - 1, 0) WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean incrementShareCount(int postId) {
        ForumSchemaInitializer.ensureSchema();
        String sql = "UPDATE forum_posts SET share_count = share_count + 1 WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasUserLikedPost(int postId, int userId) {
        ForumSchemaInitializer.ensureSchema();
        String sql = "SELECT COUNT(*) FROM forum_likes WHERE post_id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean toggleLike(int postId, int userId) {
        ForumSchemaInitializer.ensureSchema();
        boolean alreadyLiked = hasUserLikedPost(postId, userId);

        if (alreadyLiked) {
            String deleteSql = "DELETE FROM forum_likes WHERE post_id = ? AND user_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setInt(1, postId);
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
                decrementLikeCount(postId);
                return false;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            String insertSql = "INSERT INTO forum_likes (post_id, user_id) VALUES (?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, postId);
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
                incrementLikeCount(postId);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public List<ForumPost> findModerationQueue() {
        ForumSchemaInitializer.ensureSchema();
        List<ForumPost> posts = new ArrayList<>();
        String sql = """
                SELECT p.*, c.name AS category_name, u.first_name, u.last_name, u.role,
                       (SELECT COUNT(*) FROM forum_comments fc WHERE fc.post_id = p.id AND fc.status <> 'DELETED') AS calculated_comment_count,
                       (SELECT COUNT(*) FROM forum_reports fr WHERE fr.post_id = p.id AND fr.status = 'OPEN') AS report_count,
                       (SELECT COUNT(*) FROM forum_likes fl WHERE fl.post_id = p.id) AS calculated_like_count,
                       p.share_count
                FROM forum_posts p
                JOIN forum_categories c ON p.category_id = c.id
                JOIN `user` u ON p.author_id = u.id
                WHERE p.status = 'PENDING_REVIEW'
                   OR EXISTS (SELECT 1 FROM forum_reports fr WHERE fr.post_id = p.id AND fr.status = 'OPEN')
                ORDER BY report_count DESC, p.created_at DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                posts.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public boolean updateStatus(int postId, String status) {
        ForumSchemaInitializer.ensureSchema();
        String sql = "UPDATE forum_posts SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, postId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(ForumPost post) {
        ForumSchemaInitializer.ensureSchema();
        String sql = "UPDATE forum_posts SET title = ?, content = ?, image_url = ?, video_url = ?, tag = ?, updated_at = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, post.getTitle());
            pstmt.setString(2, post.getContent());
            pstmt.setString(3, post.getImageUrl());
            pstmt.setString(4, post.getVideoUrl());
            pstmt.setString(5, post.getTag());
            pstmt.setInt(6, post.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int postId) {
        ForumSchemaInitializer.ensureSchema();
        String sql = "UPDATE forum_posts SET status = 'DELETED' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ForumPost findById(int postId) {
        ForumSchemaInitializer.ensureSchema();
        String sql = """
                SELECT p.*, c.name AS category_name, u.first_name, u.last_name, u.role,
                       (SELECT COUNT(*) FROM forum_comments fc WHERE fc.post_id = p.id AND fc.status <> 'DELETED') AS calculated_comment_count,
                       (SELECT COUNT(*) FROM forum_reports fr WHERE fr.post_id = p.id AND fr.status = 'OPEN') AS report_count,
                       (SELECT COUNT(*) FROM forum_likes fl WHERE fl.post_id = p.id) AS calculated_like_count,
                       p.share_count
                FROM forum_posts p
                JOIN forum_categories c ON p.category_id = c.id
                JOIN `user` u ON p.author_id = u.id
                WHERE p.id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ForumPost mapResultSet(ResultSet rs) throws SQLException {
        ForumPost post = new ForumPost();
        post.setId(rs.getInt("id"));
        post.setCategoryId(rs.getInt("category_id"));
        post.setCategoryName(rs.getString("category_name"));
        post.setAuthorId(rs.getInt("author_id"));
        post.setAuthorName(rs.getString("first_name") + " " + rs.getString("last_name"));
        post.setAuthorRole(rs.getString("role"));
        post.setTitle(rs.getString("title"));
        post.setContent(rs.getString("content"));
        post.setLanguage(rs.getString("language"));
        post.setStatus(rs.getString("status"));
        post.setImageUrl(rs.getString("image_url"));
        post.setVideoUrl(rs.getString("video_url"));
        post.setTag(rs.getString("tag"));
        post.setUsefulCount(rs.getInt("useful_count"));
        
        // Try to get calculated counts first, fallback to column values
        try {
            post.setLikeCount(rs.getInt("calculated_like_count"));
        } catch (SQLException e) {
            post.setLikeCount(rs.getInt("like_count"));
        }
        
        post.setShareCount(rs.getInt("share_count"));
        
        try {
            post.setCommentCount(rs.getInt("calculated_comment_count"));
        } catch (SQLException e) {
            post.setCommentCount(rs.getInt("comment_count"));
        }
        
        post.setReportCount(rs.getInt("report_count"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (createdAt != null) post.setCreatedAt(createdAt.toLocalDateTime());
        if (updatedAt != null) post.setUpdatedAt(updatedAt.toLocalDateTime());
        return post;
    }
}
