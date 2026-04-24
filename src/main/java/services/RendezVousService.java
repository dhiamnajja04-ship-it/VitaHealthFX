package services;

import models.RendezVous;
import utils.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RendezVousService {

    private Connection cnx;

    public RendezVousService() throws SQLException {
        this.cnx = MyConnection.getInstance().getConnection();
    }

    // ─── Validation ────────────────────────────────────────────────────────────

    public String valider(RendezVous rv) {
        if (rv.getDate() == null)
            return "La date est obligatoire.";
        if (rv.getDate().isBefore(LocalDate.now()))
            return "La date ne peut pas être dans le passé.";

        if (rv.getHeure() == null)
            return "L'heure est obligatoire.";

        LocalTime ouverture = LocalTime.of(8, 0);
        LocalTime fermeture = LocalTime.of(18, 0);
        if (rv.getHeure().isBefore(ouverture) || rv.getHeure().isAfter(fermeture))
            return "L'heure doit être entre 08:00 et 18:00.";

        if (rv.getMotif() == null || rv.getMotif().trim().isEmpty())
            return "Le motif est obligatoire.";
        if (rv.getMotif().length() > 200)
            return "Le motif ne doit pas dépasser 200 caractères.";

        if (rv.getMedecinId() <= 0)
            return "Veuillez sélectionner un médecin.";

        if (rv.getPatientNom() == null || rv.getPatientNom().trim().isEmpty())
            return "Le nom du patient est obligatoire.";

        if (rv.getPatientPrenom() == null || rv.getPatientPrenom().trim().isEmpty())
            return "Le prénom du patient est obligatoire.";

        if (rv.getPatientTel() != null && !rv.getPatientTel().isEmpty()) {
            if (!Pattern.matches("^[0-9]{8}$", rv.getPatientTel()))
                return "Le téléphone du patient doit contenir 8 chiffres.";
        }

        String statut = rv.getStatut();
        if (statut == null || (!statut.equals("en_attente") && !statut.equals("confirme") && !statut.equals("annule")))
            return "Le statut doit être 'en_attente', 'confirme' ou 'annule'.";

        String priorite = rv.getPriorite();
        if (priorite == null || (!priorite.equals("Normale") && !priorite.equals("Urgente") && !priorite.equals("Basse")))
            return "La priorité doit être 'Normale', 'Urgente' ou 'Basse'.";

        return null;
    }

    // ─── CRUD ──────────────────────────────────────────────────────────────────

    public void ajouter(RendezVous rv) throws SQLException {
        String erreur = valider(rv);
        if (erreur != null) throw new IllegalArgumentException(erreur);

        if (creneauOccupe(rv.getMedecinId(), rv.getDate(), rv.getHeure(), -1))
            throw new IllegalArgumentException("Ce créneau est déjà pris pour ce médecin.");

        String sql = "INSERT INTO rendez_vous (date, heure, motif, statut, priorite, medecin_id, patient_nom, patient_prenom, patient_tel) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(rv.getDate()));
            ps.setTime(2, Time.valueOf(rv.getHeure()));
            ps.setString(3, rv.getMotif().trim());
            ps.setString(4, rv.getStatut());
            ps.setString(5, rv.getPriorite());
            ps.setInt(6, rv.getMedecinId());
            ps.setString(7, rv.getPatientNom().trim());
            ps.setString(8, rv.getPatientPrenom().trim());
            ps.setString(9, rv.getPatientTel());
            ps.executeUpdate();
        }
    }

    public void modifier(RendezVous rv) throws SQLException {
        String erreur = valider(rv);
        if (erreur != null) throw new IllegalArgumentException(erreur);

        if (creneauOccupe(rv.getMedecinId(), rv.getDate(), rv.getHeure(), rv.getId()))
            throw new IllegalArgumentException("Ce créneau est déjà pris pour ce médecin.");

        String sql = "UPDATE rendez_vous SET date=?, heure=?, motif=?, statut=?, priorite=?, medecin_id=?, patient_nom=?, patient_prenom=?, patient_tel=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(rv.getDate()));
            ps.setTime(2, Time.valueOf(rv.getHeure()));
            ps.setString(3, rv.getMotif().trim());
            ps.setString(4, rv.getStatut());
            ps.setString(5, rv.getPriorite());
            ps.setInt(6, rv.getMedecinId());
            ps.setString(7, rv.getPatientNom().trim());
            ps.setString(8, rv.getPatientPrenom().trim());
            ps.setString(9, rv.getPatientTel());
            ps.setInt(10, rv.getId());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM rendez_vous WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<RendezVous> afficher() throws SQLException {
        List<RendezVous> liste = new ArrayList<>();
        String sql = "SELECT * FROM rendez_vous ORDER BY date DESC, heure ASC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        }
        return liste;
    }

    public RendezVous getById(int id) throws SQLException {
        String sql = "SELECT * FROM rendez_vous WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapper(rs);
            }
        }
        return null;
    }

    public List<RendezVous> getByMedecin(int medecinId) throws SQLException {
        List<RendezVous> liste = new ArrayList<>();
        String sql = "SELECT * FROM rendez_vous WHERE medecin_id=? ORDER BY date DESC, heure ASC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, medecinId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        }
        return liste;
    }

    public List<RendezVous> getRendezVousByMedecinId(int medecinId) throws SQLException {
        return getByMedecin(medecinId);
    }

    public List<RendezVous> getByStatut(String statut) throws SQLException {
        List<RendezVous> liste = new ArrayList<>();
        String sql = "SELECT * FROM rendez_vous WHERE statut=? ORDER BY date, heure";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        }
        return liste;
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private boolean creneauOccupe(int medecinId, LocalDate date, LocalTime heure, int excludeId) throws SQLException {
        String sql = "SELECT id FROM rendez_vous WHERE medecin_id=? AND date=? AND heure=? AND id != ? AND statut = 'confirme'";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, medecinId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(heure));
            ps.setInt(4, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private RendezVous mapper(ResultSet rs) throws SQLException {
        return new RendezVous(
            rs.getInt("id"),
            rs.getDate("date").toLocalDate(),
            rs.getTime("heure").toLocalTime(),
            rs.getString("motif"),
            rs.getString("statut"),
            rs.getString("priorite"),
            rs.getInt("medecin_id"),
            rs.getString("patient_nom"),
            rs.getString("patient_prenom"),
            rs.getString("patient_tel")
        );
    }
}
