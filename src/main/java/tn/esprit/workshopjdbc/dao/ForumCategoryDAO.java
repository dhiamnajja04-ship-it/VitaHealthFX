package tn.esprit.workshopjdbc.dao;

import com.vitahealth.config.DatabaseConnection;
import tn.esprit.workshopjdbc.Entities.ForumCategory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ForumCategoryDAO {
    public List<ForumCategory> findAll() {
        ForumSchemaInitializer.ensureSchema();
        List<ForumCategory> categories = new ArrayList<>();
        String sql = "SELECT * FROM forum_categories ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ForumCategory category = new ForumCategory();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
}
