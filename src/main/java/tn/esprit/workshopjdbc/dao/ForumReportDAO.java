package tn.esprit.workshopjdbc.dao;

import com.vitahealth.config.DatabaseConnection;
import tn.esprit.workshopjdbc.Entities.ForumReport;

import java.sql.*;

public class ForumReportDAO {
    public boolean create(ForumReport report) {
        ForumSchemaInitializer.ensureSchema();
        String sql = "INSERT INTO forum_reports (post_id, reporter_id, reason, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, report.getPostId());
            pstmt.setInt(2, report.getReporterId());
            pstmt.setString(3, report.getReason());
            pstmt.setString(4, report.getStatus());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) report.setId(rs.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean closeReportsForPost(int postId) {
        ForumSchemaInitializer.ensureSchema();
        String sql = "UPDATE forum_reports SET status = 'CLOSED' WHERE post_id = ? AND status = 'OPEN'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
