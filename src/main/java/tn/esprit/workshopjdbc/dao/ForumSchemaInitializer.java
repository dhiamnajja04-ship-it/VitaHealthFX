package tn.esprit.workshopjdbc.dao;

import com.vitahealth.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class ForumSchemaInitializer {
    private static boolean initialized = false;

    private ForumSchemaInitializer() {}

    public static synchronized void ensureSchema() {
        if (initialized) return;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS forum_categories (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(80) NOT NULL UNIQUE,
                        description VARCHAR(255),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS forum_posts (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        category_id INT NOT NULL,
                        author_id INT NOT NULL,
                        title VARCHAR(180) NOT NULL,
                        content TEXT NOT NULL,
                        language VARCHAR(10) DEFAULT 'fr',
                        status VARCHAR(30) DEFAULT 'PUBLISHED',
                        useful_count INT DEFAULT 0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT fk_forum_posts_category FOREIGN KEY (category_id) REFERENCES forum_categories(id),
                        CONSTRAINT fk_forum_posts_author FOREIGN KEY (author_id) REFERENCES `user`(id)
                    )
                    """);

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS forum_comments (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        post_id INT NOT NULL,
                        author_id INT NOT NULL,
                        content TEXT NOT NULL,
                        status VARCHAR(30) DEFAULT 'PUBLISHED',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_forum_comments_post FOREIGN KEY (post_id) REFERENCES forum_posts(id) ON DELETE CASCADE,
                        CONSTRAINT fk_forum_comments_author FOREIGN KEY (author_id) REFERENCES `user`(id)
                    )
                    """);

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS forum_reports (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        post_id INT NOT NULL,
                        reporter_id INT NOT NULL,
                        reason VARCHAR(255) NOT NULL,
                        status VARCHAR(30) DEFAULT 'OPEN',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_forum_reports_post FOREIGN KEY (post_id) REFERENCES forum_posts(id) ON DELETE CASCADE,
                        CONSTRAINT fk_forum_reports_user FOREIGN KEY (reporter_id) REFERENCES `user`(id)
                    )
                    """);

            seedCategories(stmt);
            initialized = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void seedCategories(Statement stmt) throws SQLException {
        stmt.executeUpdate("INSERT IGNORE INTO forum_categories (name, description) VALUES " +
                "('Diabete', 'Echanges autour du diabete et de la glycemie')," +
                "('Hypertension', 'Questions et conseils sur la tension arterielle')," +
                "('Nutrition', 'Alimentation, regimes et habitudes sante')," +
                "('Sante mentale', 'Bien-etre, stress et accompagnement')," +
                "('Questions medecins', 'Questions destinees aux medecins verifies')," +
                "('Experiences patients', 'Temoignages et parcours de soins')," +
                "('Actualites medicales', 'Informations et nouveautes sante')");
    }
}
