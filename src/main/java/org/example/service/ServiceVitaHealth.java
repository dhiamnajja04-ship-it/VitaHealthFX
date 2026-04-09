package org.example.service;

import org.example.entity.User;
import org.example.entity.Appointment;
import org.example.DAO.UserDAO;
import org.example.DAO.AppointmentDAO;
import java.sql.SQLException;
import java.util.List;

public class ServiceVitaHealth {
    private UserDAO userDAO;
    private AppointmentDAO appointmentDAO;

    public ServiceVitaHealth() {
        this.userDAO = new UserDAO();
        this.appointmentDAO = new AppointmentDAO();
    }

    // ========== User CRUD ==========
    public void inscrirePatient(User patient) throws SQLException {
        if (userDAO.findByEmail(patient.getEmail()) != null) {
            throw new SQLException("Email déjà utilisé");
        }
        patient.setRole("patient");
        userDAO.ajouter(patient);
    }

    public void inscrireMedecin(User medecin) throws SQLException {
        if (userDAO.findByEmail(medecin.getEmail()) != null) {
            throw new SQLException("Email déjà utilisé");
        }
        medecin.setRole("medecin");
        medecin.setVerified(false);
        userDAO.ajouter(medecin);
    }

    public void inscrireAdmin(User admin) throws SQLException {
        if (userDAO.findByEmail(admin.getEmail()) != null) {
            throw new SQLException("Email déjà utilisé");
        }
        admin.setRole("admin");
        admin.setVerified(true);
        userDAO.ajouter(admin);
    }

    public User login(String email, String password) throws SQLException {
        User user = userDAO.findByEmail(email);
        if (user != null && password.equals(user.getPassword())) {
            return user;
        }
        return null;
    }

    // READ - User
    public User getUserById(int id) throws SQLException {
        return userDAO.findById(id);
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }

    public List<User> getUsersByRole(String role) throws SQLException {
        return userDAO.findByRole(role);
    }

    public List<User> getAllMedecins() throws SQLException {
        return userDAO.getAllMedecins();
    }

    // ========== RECHERCHE ==========

    public List<User> rechercherUtilisateursParNom(String nom) throws SQLException {
        return userDAO.rechercherParNom(nom);
    }

    public List<User> rechercherUtilisateursParEmail(String email) throws SQLException {
        return userDAO.rechercherParEmail(email);
    }

    public List<User> rechercherUtilisateursParRole(String role) throws SQLException {
        return userDAO.rechercherParRole(role);
    }

    public List<User> rechercherMedecinsParSpecialite(String specialite) throws SQLException {
        return userDAO.rechercherParSpecialite(specialite);
    }

    // UPDATE - User
    public void updateUser(User user) throws SQLException {
        userDAO.update(user);
    }

    public void updatePatientHealth(int patientId, Double poids, Double taille, Double glycemie, String tension) throws SQLException {
        userDAO.updateHealthParameters(patientId, poids, taille, glycemie, tension);
    }

    // DELETE - User
    public void deleteUser(int id) throws SQLException {
        userDAO.delete(id);
    }

    // ========== Appointment CRUD ==========
    public void prendreRendezVous(Appointment rdv) throws SQLException {
        appointmentDAO.ajouter(rdv);
    }

    public List<Appointment> getRdvByPatient(int patientId) throws SQLException {
        return appointmentDAO.getRdvByPatient(patientId);
    }

    public List<Appointment> getRdvByMedecin(int medecinId) throws SQLException {
        return appointmentDAO.getRdvByMedecin(medecinId);
    }

    public void updateAppointmentStatus(int rdvId, String status) throws SQLException {
        appointmentDAO.updateStatus(rdvId, status);
    }

    // ========== IA Recommandation ==========
    public String getRecommendationIA(User patient) {
        StringBuilder recommandation = new StringBuilder();

        if (patient.getPoids() != null && patient.getTaille() != null) {
            double imc = patient.getPoids() / (patient.getTaille() * patient.getTaille());
            if (imc > 25) {
                recommandation.append("⚠️ IMC élevé (").append(String.format("%.1f", imc)).append(") : Pensez à faire plus d'exercice. ");
            } else if (imc < 18.5) {
                recommandation.append("⚠️ IMC bas (").append(String.format("%.1f", imc)).append(") : Surveillez votre alimentation. ");
            } else {
                recommandation.append("✅ IMC normal (").append(String.format("%.1f", imc)).append("). ");
            }
        }

        if (patient.getGlycemie() != null) {
            if (patient.getGlycemie() > 1.26) {
                recommandation.append("🩸 Glycémie élevée (").append(patient.getGlycemie()).append(") : Consultez votre médecin. ");
            } else if (patient.getGlycemie() < 0.70) {
                recommandation.append("🩸 Glycémie basse (").append(patient.getGlycemie()).append(") : Mangez quelque chose de sucré. ");
            } else {
                recommandation.append("✅ Glycémie normale. ");
            }
        }

        if (recommandation.length() == 0) {
            return "✅ Tous vos paramètres sont bons ! Continuez ainsi.";
        }
        return recommandation.toString();
    }
}