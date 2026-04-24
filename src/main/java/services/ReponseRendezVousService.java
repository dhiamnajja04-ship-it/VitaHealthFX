package services;

import models.ReponseRendezVous;
import utils.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReponseRendezVousService {

    private Connection cnx;

    public ReponseRendezVousService() throws SQLException {
        this.cnx = MyConnection.getInstance().getConnection();
    }

    // ─── Validation ────────────────────────────────────────────────────────────

    public String valider(ReponseRendezVous r) {
        if (r.getRendezVousId() <= 0)
            return "Un rendez-vous valide est requis.";

        if (r.getMessage() == null || r.getMessage().trim().isEmpty())
            return "Le message de réponse est obligatoire.";
        if (r.getMessage().length() > 500)
            return "Le message ne doit pas dépasser 500 caractères.";

        String type = r.getTypeReponse();
        if (type == null || (!type.equals("accepte") && !type.equals("refuse") && !type.equals("reporte")))
            return "Le type doit être 'accepte', 'refuse' ou 'reporte'.";

        return null;
    }

    // ─── CRUD ──────────────────────────────────────────────────────────────────

    public void ajouter(ReponseRendezVous r) throws SQLException {
        String erreur = valider(r);
        if (erreur != null) throw new IllegalArgumentException(erreur);

        // Suppression de la contrainte de réponse unique pour permettre l'historique

        r.setDateReponse(LocalDateTime.now());

        String sql = "INSERT INTO reponse_rendez_vous (message, date_reponse, rendez_vous_id, type_reponse) VALUES (?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, r.getMessage().trim());
            ps.setTimestamp(2, Timestamp.valueOf(r.getDateReponse()));
            ps.setInt(3, r.getRendezVousId());
            ps.setString(4, r.getTypeReponse());
            ps.executeUpdate();
        }

        mettreAJourStatutRdv(r.getRendezVousId(), r.getTypeReponse());
    }

    public void modifier(ReponseRendezVous r) throws SQLException {
        String erreur = valider(r);
        if (erreur != null) throw new IllegalArgumentException(erreur);

        // Suppression de la contrainte de réponse unique pour permettre l'historique

        String sql = "UPDATE reponse_rendez_vous SET message=?, type_reponse=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, r.getMessage().trim());
            ps.setString(2, r.getTypeReponse());
            ps.setInt(3, r.getId());
            ps.executeUpdate();
        }

        mettreAJourStatutRdv(r.getRendezVousId(), r.getTypeReponse());
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM reponse_rendez_vous WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<ReponseRendezVous> afficher() throws SQLException {
        List<ReponseRendezVous> liste = new ArrayList<>();
        String sql = "SELECT * FROM reponse_rendez_vous ORDER BY date_reponse DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        }
        return liste;
    }

    public ReponseRendezVous getById(int id) throws SQLException {
        String sql = "SELECT * FROM reponse_rendez_vous WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapper(rs);
            }
        }
        return null;
    }

    public List<ReponseRendezVous> getReponsesByRendezVousId(int rendezVousId) throws SQLException {
        List<ReponseRendezVous> liste = new ArrayList<>();
        String sql = "SELECT * FROM reponse_rendez_vous WHERE rendez_vous_id=? ORDER BY date_reponse DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, rendezVousId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        }
        return liste;
    }

    public List<ReponseRendezVous> getReponsesByMedecinId(int medecinId) throws SQLException {
        List<ReponseRendezVous> liste = new ArrayList<>();
        String sql = "SELECT rep.* FROM reponse_rendez_vous rep " +
                     "JOIN rendez_vous rdv ON rep.rendez_vous_id = rdv.id " +
                     "WHERE rdv.medecin_id = ? " +
                     "ORDER BY rep.date_reponse DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, medecinId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        }
        return liste;
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private boolean existeDejaReponse(int rendezVousId, int excludeId) throws SQLException {
        String sql = "SELECT id FROM reponse_rendez_vous WHERE rendez_vous_id=? AND id != ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, rendezVousId);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void mettreAJourStatutRdv(int rendezVousId, String typeReponse) throws SQLException {
        if ("accepte".equals(typeReponse)) {
            // Vérifier si le créneau est déjà pris par un autre RDV confirmé
            String checkSql = "SELECT r1.id FROM rendez_vous r1 " +
                             "JOIN rendez_vous r2 ON r1.medecin_id = r2.medecin_id " +
                             "AND r1.date = r2.date AND r1.heure = r2.heure " +
                             "WHERE r2.id = ? AND r1.id != r2.id AND r1.statut = 'confirme'";
            try (PreparedStatement ps = cnx.prepareStatement(checkSql)) {
                ps.setInt(1, rendezVousId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new SQLException("Impossible de confirmer : ce créneau est déjà réservé par un autre rendez-vous.");
                    }
                }
            }
        }

        String statut = switch (typeReponse) {
            case "accepte" -> "confirme";
            case "refuse"  -> "annule";
            default        -> "en_attente";
        };
        
        String sql = "UPDATE rendez_vous SET statut=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, rendezVousId);
            ps.executeUpdate();
        }
    }

    private ReponseRendezVous mapper(ResultSet rs) throws SQLException {
        return new ReponseRendezVous(
            rs.getInt("id"),
            rs.getString("message"),
            rs.getTimestamp("date_reponse").toLocalDateTime(),
            rs.getInt("rendez_vous_id"),
            rs.getString("type_reponse")
        );
    }
}
