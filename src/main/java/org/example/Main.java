package org.example;

import org.example.entity.User;
import org.example.entity.Appointment;
import org.example.service.ServiceVitaHealth;
import org.example.config.DatabaseConnection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ServiceVitaHealth service = new ServiceVitaHealth();

        try {
            // Génération d'un suffixe unique basé sur le timestamp
            String uniqueSuffix = String.valueOf(System.currentTimeMillis());

            // 1. Inscription patient avec email unique
            User patient = new User();
            patient.setEmail("patient_" + uniqueSuffix + "@test.com");
            patient.setPassword("password123");
            patient.setFirstName("Wael");
            patient.setLastName("Tounsi");
            patient.setPoids(70.0);
            patient.setTaille(1.75);
            patient.setGlycemie(0.95);
            patient.setTension("12/8");

            service.inscrirePatient(patient);
            System.out.println("✅ Patient inscrit avec ID: " + patient.getId());

            // 2. Inscription médecin avec email unique
            User medecin = new User();
            medecin.setEmail("medecin_" + uniqueSuffix + "@test.com");
            medecin.setPassword("doctor123");
            medecin.setFirstName("John");
            medecin.setLastName("Smith");
            medecin.setSpecialite("Cardiologie");
            medecin.setDiplome("diplome.pdf");
            medecin.setCin("12345678");

            service.inscrireMedecin(medecin);
            System.out.println("✅ Médecin inscrit avec ID: " + medecin.getId());

            // 3. Authentification
            User loggedUser = service.login(patient.getEmail(), "password123");
            if (loggedUser != null) {
                System.out.println("✅ Authentification réussie: " + loggedUser.getFirstName());
            }

            // 4. Recommandation IA
            String recommendation = service.getRecommendationIA(patient);
            System.out.println("🤖 Recommandation IA: " + recommendation);

            // 5. Rendez-vous (avec les bonnes colonnes : doctor_id, date, reason)
            Appointment rdv = new Appointment();
            rdv.setPatientId(patient.getId());
            rdv.setDoctorId(medecin.getId());          // ← doctorId au lieu de medecinId
            rdv.setDate(LocalDateTime.now().plusDays(2)); // ← date au lieu de dateTime
            rdv.setReason("Consultation de routine");   // ← reason au lieu de motif

            service.prendreRendezVous(rdv);
            System.out.println("✅ Rendez-vous créé avec ID: " + rdv.getId());

            // ========== 6. Tests des nouvelles méthodes CRUD ==========
            System.out.println("\n=== TEST CRUD UTILISATEURS ===");

            // 6.1 Récupérer l'utilisateur par son ID
            User fetched = service.getUserById(patient.getId());
            System.out.println("🔍 Utilisateur trouvé : " + fetched.getFirstName() + " " + fetched.getLastName());

            // 6.2 Lister tous les utilisateurs
            System.out.println("📋 Liste de tous les utilisateurs :");
            List<User> allUsers = service.getAllUsers();
            for (User u : allUsers) {
                System.out.println("   - " + u.getEmail() + " (" + u.getRole() + ")");
            }

            // 6.3 Lister tous les médecins
            System.out.println("👨‍⚕️ Médecins :");
            List<User> medecins = service.getAllMedecins();
            for (User m : medecins) {
                System.out.println("   - Dr. " + m.getLastName() + " (" + m.getSpecialite() + ")");
            }

            // 6.4 Mettre à jour les paramètres santé du patient
            service.updatePatientHealth(patient.getId(), 72.5, 1.76, 1.02, "13/8");
            System.out.println("🩺 Paramètres santé mis à jour pour " + patient.getFirstName());

            // 6.5 (Optionnel) Supprimer le patient – décommente pour tester
            // service.deleteUser(patient.getId());
            // System.out.println("🗑️ Patient supprimé (ID " + patient.getId() + ")");

        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}