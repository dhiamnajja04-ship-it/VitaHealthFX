package org.example.DAO;

import org.example.entity.ForumSujet;
import org.example.entity.Commentaire;
import org.example.config.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ForumDAO {

    // Ajouter un sujet
    public void ajouterSujet(ForumSujet sujet) throws SQLException {
        String sql = "INSERT INTO forum (auteur_id, title, content) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, sujet.getAuteurId());
            pstmt.setString(2, sujet.getTitle());
            pstmt.setString(3, sujet.getContent());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) sujet.setId(rs.getInt(1));
            }
        }
    }

    // Récupérer tous les sujets
    public List<ForumSujet> getAllSujets() throws SQLException {
        List<ForumSujet> sujets = new ArrayList<>();
        String sql = "SELECT f.*, u.first_name, u.last_name FROM forum f " +
                "JOIN user u ON f.auteur_id = u.id ORDER BY f.created_at DESC";
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ForumSujet sujet = new ForumSujet();
                sujet.setId(rs.getInt("id"));
                sujet.setAuteurId(rs.getInt("auteur_id"));
                sujet.setAuteurNom(rs.getString("first_name") + " " + rs.getString("last_name"));
                sujet.setTitle(rs.getString("title"));
                sujet.setContent(rs.getString("content"));
                sujet.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                sujets.add(sujet);
            }
        }
        return sujets;
    }

    // Ajouter un commentaire
    public void ajouterCommentaire(Commentaire commentaire) throws SQLException {
        String sql = "INSERT INTO commentaire (forum_id, auteur_id, content) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, commentaire.getForumId());
            pstmt.setInt(2, commentaire.getAuteurId());
            pstmt.setString(3, commentaire.getContent());
            pstmt.executeUpdate();
        }
    }

    // Récupérer les commentaires d'un sujet
    public List<Commentaire> getCommentairesBySujet(int forumId) throws SQLException {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT c.*, u.first_name, u.last_name FROM commentaire c " +
                "JOIN user u ON c.auteur_id = u.id WHERE c.forum_id = ? ORDER BY c.created_at ASC";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, forumId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setId(rs.getInt("id"));
                c.setForumId(rs.getInt("forum_id"));
                c.setAuteurId(rs.getInt("auteur_id"));
                c.setAuteurNom(rs.getString("first_name") + " " + rs.getString("last_name"));
                c.setContent(rs.getString("content"));
                c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                commentaires.add(c);
            }
        }
        return commentaires;
    }
}