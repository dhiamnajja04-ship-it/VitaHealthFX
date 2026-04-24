package services;

import models.Medecin;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MedecinService {

    private Connection cnx;

    public MedecinService() throws SQLException {
        this.cnx = MyConnection.getInstance().getConnection();
    }

    // ─── Validation ────────────────────────────────────────────────────────────

    public String valider(Medecin m) {
        if (m.getNom() == null || m.getNom().trim().isEmpty())
            return "Le nom est obligatoire.";
        if (m.getNom().length() > 50)
            return "Le nom ne doit pas dépasser 50 caractères.";

        if (m.getPrenom() == null || m.getPrenom().trim().isEmpty())
            return "Le prénom est obligatoire.";
        if (m.getPrenom().length() > 50)
            return "Le prénom ne doit pas dépasser 50 caractères.";

        if (m.getSpecialite() == null || m.getSpecialite().trim().isEmpty())
            return "La spécialité est obligatoire.";

        if (m.getTelephone() != null && !m.getTelephone().isEmpty()) {
            if (!Pattern.matches("^[0-9]{8}$", m.getTelephone()))
                return "Le téléphone doit contenir exactement 8 chiffres.";
        }

        if (m.getEmail() != null && !m.getEmail().isEmpty()) {
            if (!Pattern.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", m.getEmail()))
                return "L'adresse email est invalide.";
        }

        return null;
    }

    // ─── CRUD ──────────────────────────────────────────────────────────────────

    public void ajouter(Medecin m) throws SQLException {
        String erreur = valider(m);
        if (erreur != null) throw new IllegalArgumentException(erreur);

        if (existeParEmail(m.getEmail(), -1))
            throw new IllegalArgumentException("Un médecin avec cet email existe déjà.");

        String sql = "INSERT INTO medecin (nom, prenom, specialite, telephone, email) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, m.getNom().trim());
            ps.setString(2, m.getPrenom().trim());
            ps.setString(3, m.getSpecialite().trim());
            ps.setString(4, m.getTelephone());
            ps.setString(5, m.getEmail());
            ps.executeUpdate();
        }
    }

    public void modifier(Medecin m) throws SQLException {
        String erreur = valider(m);
        if (erreur != null) throw new IllegalArgumentException(erreur);

        if (existeParEmail(m.getEmail(), m.getId()))
            throw new IllegalArgumentException("Cet email est déjà utilisé par un autre médecin.");

        String sql = "UPDATE medecin SET nom=?, prenom=?, specialite=?, telephone=?, email=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, m.getNom().trim());
            ps.setString(2, m.getPrenom().trim());
            ps.setString(3, m.getSpecialite().trim());
            ps.setString(4, m.getTelephone());
            ps.setString(5, m.getEmail());
            ps.setInt(6, m.getId());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM medecin WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Medecin> afficher() throws SQLException {
        List<Medecin> liste = new ArrayList<>();
        String sql = "SELECT * FROM medecin ORDER BY nom, prenom";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapper(rs));
            }
        }
        return liste;
    }

    public Medecin getById(int id) throws SQLException {
        String sql = "SELECT * FROM medecin WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapper(rs);
            }
        }
        return null;
    }

    public List<Medecin> rechercher(String motCle) throws SQLException {
        List<Medecin> liste = new ArrayList<>();
        String sql = "SELECT * FROM medecin WHERE nom LIKE ? OR prenom LIKE ? OR specialite LIKE ?";
        String pattern = "%" + motCle + "%";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        }
        return liste;
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private boolean existeParEmail(String email, int excludeId) throws SQLException {
        if (email == null || email.isEmpty()) return false;
        String sql = "SELECT id FROM medecin WHERE email=? AND id != ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Medecin mapper(ResultSet rs) throws SQLException {
        return new Medecin(
            rs.getInt("id"),
            rs.getString("nom"),
            rs.getString("prenom"),
            rs.getString("specialite"),
            rs.getString("telephone"),
            rs.getString("email")
        );
    }
}
