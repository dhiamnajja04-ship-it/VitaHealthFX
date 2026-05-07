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
                        image_url VARCHAR(500),
                        video_url VARCHAR(500),
                        tag VARCHAR(50),
                        useful_count INT DEFAULT 0,
                        like_count INT DEFAULT 0,
                        share_count INT DEFAULT 0,
                        comment_count INT DEFAULT 0,
                        report_count INT DEFAULT 0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT fk_forum_posts_category FOREIGN KEY (category_id) REFERENCES forum_categories(id),
                        CONSTRAINT fk_forum_posts_author FOREIGN KEY (author_id) REFERENCES `user`(id)
                    )
                    """);

            // Add new columns if table already exists (for existing databases)
            try {
                stmt.executeUpdate("ALTER TABLE forum_posts ADD COLUMN IF NOT EXISTS image_url VARCHAR(500)");
                stmt.executeUpdate("ALTER TABLE forum_posts ADD COLUMN IF NOT EXISTS video_url VARCHAR(500)");
                stmt.executeUpdate("ALTER TABLE forum_posts ADD COLUMN IF NOT EXISTS tag VARCHAR(50)");
                stmt.executeUpdate("ALTER TABLE forum_posts ADD COLUMN IF NOT EXISTS like_count INT DEFAULT 0");
                stmt.executeUpdate("ALTER TABLE forum_posts ADD COLUMN IF NOT EXISTS share_count INT DEFAULT 0");
                stmt.executeUpdate("ALTER TABLE forum_posts ADD COLUMN IF NOT EXISTS comment_count INT DEFAULT 0");
                stmt.executeUpdate("ALTER TABLE forum_posts ADD COLUMN IF NOT EXISTS report_count INT DEFAULT 0");
            } catch (SQLException e) {
                // Columns might already exist, ignore
            }

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS forum_comments (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        post_id INT NOT NULL,
                        parent_comment_id INT,
                        author_id INT NOT NULL,
                        content TEXT NOT NULL,
                        status VARCHAR(30) DEFAULT 'PUBLISHED',
                        like_count INT DEFAULT 0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_forum_comments_post FOREIGN KEY (post_id) REFERENCES forum_posts(id) ON DELETE CASCADE,
                        CONSTRAINT fk_forum_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES forum_comments(id) ON DELETE CASCADE,
                        CONSTRAINT fk_forum_comments_author FOREIGN KEY (author_id) REFERENCES `user`(id)
                    )
                    """);

            // Add new columns if table already exists (for existing databases)
            try {
                stmt.executeUpdate("ALTER TABLE forum_comments ADD COLUMN IF NOT EXISTS parent_comment_id INT");
                stmt.executeUpdate("ALTER TABLE forum_comments ADD COLUMN IF NOT EXISTS like_count INT DEFAULT 0");
                stmt.executeUpdate("ALTER TABLE forum_comments ADD CONSTRAINT IF NOT EXISTS fk_forum_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES forum_comments(id) ON DELETE CASCADE");
            } catch (SQLException e) {
                // Columns might already exist, ignore
            }

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

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS forum_likes (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        post_id INT NOT NULL,
                        user_id INT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE KEY unique_post_user (post_id, user_id),
                        CONSTRAINT fk_forum_likes_post FOREIGN KEY (post_id) REFERENCES forum_posts(id) ON DELETE CASCADE,
                        CONSTRAINT fk_forum_likes_user FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
                    )
                    """);

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS forum_comment_likes (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        comment_id INT NOT NULL,
                        user_id INT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE KEY unique_comment_user (comment_id, user_id),
                        CONSTRAINT fk_forum_comment_likes_comment FOREIGN KEY (comment_id) REFERENCES forum_comments(id) ON DELETE CASCADE,
                        CONSTRAINT fk_forum_comment_likes_user FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
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
