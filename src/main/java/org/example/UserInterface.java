package org.example;

import org.example.entity.User;
import org.example.entity.Appointment;
import org.example.service.ServiceVitaHealth;
import org.example.config.DatabaseConnection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.HashSet;
import java.util.Set;

public class UserInterface {
    private static ServiceVitaHealth service = new ServiceVitaHealth();
    private static Scanner scanner = new Scanner(System.in);
    private static User currentUser = null;
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║            🌟 BIENVENUE SUR VITAHEALTH 🌟                ║");
        System.out.println("║         Plateforme de suivi médical intelligent         ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        while (true) {
            try {
                if (currentUser == null) {
                    showMenuNonConnecte();
                } else {
                    showMenuConnecte();
                }
            } catch (SQLException e) {
                System.err.println("❌ Erreur base de données : " + e.getMessage());
            }
        }
    }

    // ==================== MENU NON CONNECTÉ ====================
    private static void showMenuNonConnecte() throws SQLException {
        System.out.println("\n┌───────────── MENU PRINCIPAL ─────────────┐");
        System.out.println("│ 1. 🔐 Se connecter                       │");
        System.out.println("│ 2. 📝 S'inscrire (Patient)               │");
        System.out.println("│ 3. 👨‍⚕️ S'inscrire (Médecin)              │");
        System.out.println("│ 4. 👑 Créer compte Admin                 │");
        System.out.println("│ 5. ❌ Quitter                            │");
        System.out.println("└──────────────────────────────────────────┘");
        System.out.print("👉 Votre choix : ");

        int choix = lireEntier();
        switch (choix) {
            case 1: login(); break;
            case 2: inscrirePatient(); break;
            case 3: inscrireMedecin(); break;
            case 4: inscrireAdmin(); break;
            case 5:
                System.out.println("👋 Au revoir et prenez soin de vous !");
                DatabaseConnection.closeConnection();
                System.exit(0);
                break;
            default: System.out.println("❌ Choix invalide !");
        }
    }

    // ==================== MENU CONNECTÉ ====================
    private static void showMenuConnecte() throws SQLException {
        System.out.println("\n┌─────────────────────────────────────────────────┐");
        System.out.println("│ Bonjour " + currentUser.getFirstName() + " " + currentUser.getLastName() + " (" + currentUser.getRole() + ")");
        System.out.println("├─────────────────────────────────────────────────┤");
        System.out.println("│ 1.  👤 Voir mon profil                          │");
        System.out.println("│ 2.  📝 Modifier mon profil                      │");
        System.out.println("│ 3.  🩺 Modifier mes paramètres santé            │");
        System.out.println("│ 4.  🤖 Recommandation IA                        │");
        System.out.println("│ 5.  📅 Prendre rendez-vous                      │");
        System.out.println("│ 6.  📋 Mes rendez-vous                          │");
        System.out.println("│ 7.  👨‍⚕️ Voir tous les médecins                  │");

        if (currentUser.getRole().equals("medecin")) {
            System.out.println("│ 8.  👥 Mes patients (Médecin)                  │");
            System.out.println("│ 9.  ✅ Gérer mes rendez-vous (Médecin)         │");
            System.out.println("│ 10. 🔍 Rechercher patient (Médecin)            │");
        }

        if (currentUser.getRole().equals("admin")) {
            System.out.println("│ 8.  📊 Tous les utilisateurs (Admin)           │");
            System.out.println("│ 9.  🗑️ Supprimer un utilisateur (Admin)        │");
            System.out.println("│ 10. 🔓 Activer/Désactiver un utilisateur       │");
            System.out.println("│ 11. 🔍 Rechercher utilisateurs (Admin)         │");
        }

        System.out.println("├─────────────────────────────────────────────────┤");
        System.out.println("│ 0.  🚪 Se déconnecter                           │");
        System.out.println("│ 99. ❌ Quitter                                   │");
        System.out.println("└─────────────────────────────────────────────────┘");
        System.out.print("👉 Votre choix : ");

        int choix = lireEntier();

        if (choix == 0) {
            currentUser = null;
            System.out.println("✅ Déconnecté avec succès !");
            return;
        }
        if (choix == 99) {
            System.out.println("👋 Au revoir !");
            DatabaseConnection.closeConnection();
            System.exit(0);
        }

        // Menu patient
        if (choix >= 1 && choix <= 7) {
            switch (choix) {
                case 1: voirProfil(); break;
                case 2: modifierProfil(); break;
                case 3: modifierParametresSante(); break;
                case 4: recommandationIA(); break;
                case 5: prendreRendezVous(); break;
                case 6: mesRendezVous(); break;
                case 7: voirTousMedecins(); break;
            }
        }
        // Menu médecin
        else if (currentUser.getRole().equals("medecin")) {
            switch (choix) {
                case 8: mesPatients(); break;
                case 9: gererRendezVousMedecin(); break;
                case 10: menuRechercheMedecin(); break;
                default: System.out.println("❌ Choix invalide !");
            }
        }
        // Menu admin
        else if (currentUser.getRole().equals("admin")) {
            switch (choix) {
                case 8: listerTousUtilisateurs(); break;
                case 9: supprimerUtilisateur(); break;
                case 10: activerDesactiverUtilisateur(); break;
                case 11: menuRechercheAdmin(); break;
                default: System.out.println("❌ Choix invalide !");
            }
        }
        else {
            System.out.println("❌ Choix invalide !");
        }
    }

    // ==================== AUTHENTIFICATION ====================

    private static void login() throws SQLException {
        System.out.print("📧 Email : ");
        String email = scanner.nextLine();
        System.out.print("🔒 Mot de passe : ");
        String password = scanner.nextLine();

        currentUser = service.login(email, password);
        if (currentUser != null) {
            System.out.println("✅ Connexion réussie ! Bienvenue " + currentUser.getFirstName());
        } else {
            System.out.println("❌ Email ou mot de passe incorrect !");
        }
    }

    private static void inscrirePatient() throws SQLException {
        User patient = new User();
        System.out.println("\n--- INSCRIPTION PATIENT ---");
        System.out.print("📧 Email : "); patient.setEmail(scanner.nextLine());
        System.out.print("🔒 Mot de passe : "); patient.setPassword(scanner.nextLine());
        System.out.print("👤 Prénom : "); patient.setFirstName(scanner.nextLine());
        System.out.print("👤 Nom : "); patient.setLastName(scanner.nextLine());
        System.out.print("⚖️ Poids (kg) : "); patient.setPoids(lireDouble());
        System.out.print("📏 Taille (m) : "); patient.setTaille(lireDouble());
        System.out.print("🩸 Glycémie : "); patient.setGlycemie(lireDouble());
        System.out.print("💓 Tension : "); patient.setTension(scanner.nextLine());

        service.inscrirePatient(patient);
        System.out.println("✅ Patient inscrit avec succès ! ID : " + patient.getId());
    }

    private static void inscrireMedecin() throws SQLException {
        User medecin = new User();
        System.out.println("\n--- INSCRIPTION MÉDECIN ---");
        System.out.print("📧 Email : "); medecin.setEmail(scanner.nextLine());
        System.out.print("🔒 Mot de passe : "); medecin.setPassword(scanner.nextLine());
        System.out.print("👤 Prénom : "); medecin.setFirstName(scanner.nextLine());
        System.out.print("👤 Nom : "); medecin.setLastName(scanner.nextLine());
        System.out.print("🏥 Spécialité : "); medecin.setSpecialite(scanner.nextLine());
        System.out.print("📜 Diplôme : "); medecin.setDiplome(scanner.nextLine());
        System.out.print("🆔 CIN : "); medecin.setCin(scanner.nextLine());

        service.inscrireMedecin(medecin);
        System.out.println("✅ Médecin inscrit avec succès ! ID : " + medecin.getId());
    }

    private static void inscrireAdmin() throws SQLException {
        User admin = new User();
        System.out.println("\n--- INSCRIPTION ADMIN ---");
        System.out.print("📧 Email : "); admin.setEmail(scanner.nextLine());
        System.out.print("🔒 Mot de passe : "); admin.setPassword(scanner.nextLine());
        System.out.print("👤 Prénom : "); admin.setFirstName(scanner.nextLine());
        System.out.print("👤 Nom : "); admin.setLastName(scanner.nextLine());

        service.inscrireAdmin(admin);
        System.out.println("✅ Admin inscrit avec succès ! ID : " + admin.getId());
    }

    // ==================== PROFIL ====================

    private static void voirProfil() throws SQLException {
        User user = service.getUserById(currentUser.getId());
        System.out.println("\n┌────────────────────────────────────┐");
        System.out.println("│         📋 MON PROFIL               │");
        System.out.println("├────────────────────────────────────┤");
        System.out.println("│ 🆔 ID         : " + user.getId());
        System.out.println("│ 📧 Email      : " + user.getEmail());
        System.out.println("│ 👤 Nom        : " + user.getFirstName() + " " + user.getLastName());
        System.out.println("│ 👑 Rôle       : " + user.getRole());
        System.out.println("│ 🏥 Spécialité : " + (user.getSpecialite() != null ? user.getSpecialite() : "Non renseignée"));
        System.out.println("│ 🆔 CIN        : " + (user.getCin() != null ? user.getCin() : "Non renseigné"));
        System.out.println("├───────────── SANTÉ ─────────────┤");
        System.out.println("│ ⚖️ Poids      : " + (user.getPoids() != null ? user.getPoids() + " kg" : "Non renseigné"));
        System.out.println("│ 📏 Taille     : " + (user.getTaille() != null ? user.getTaille() + " m" : "Non renseignée"));
        System.out.println("│ 🩸 Glycémie   : " + (user.getGlycemie() != null ? user.getGlycemie() : "Non renseignée"));
        System.out.println("│ 💓 Tension    : " + (user.getTension() != null ? user.getTension() : "Non renseignée"));
        System.out.println("│ 🩺 Maladie    : " + (user.getMaladie() != null ? user.getMaladie() : "Aucune"));
        System.out.println("└────────────────────────────────────┘");
    }

    private static void modifierProfil() throws SQLException {
        User user = service.getUserById(currentUser.getId());
        System.out.println("\n--- MODIFIER MON PROFIL ---");
        System.out.print("Nouveau prénom (" + user.getFirstName() + ") : ");
        String input = scanner.nextLine();
        if (!input.isEmpty()) user.setFirstName(input);

        System.out.print("Nouveau nom (" + user.getLastName() + ") : ");
        input = scanner.nextLine();
        if (!input.isEmpty()) user.setLastName(input);

        System.out.print("Nouveau mot de passe (laisser vide) : ");
        input = scanner.nextLine();
        if (!input.isEmpty()) user.setPassword(input);

        service.updateUser(user);
        currentUser = user;
        System.out.println("✅ Profil mis à jour !");
    }

    private static void modifierParametresSante() throws SQLException {
        System.out.println("\n--- MODIFIER PARAMÈTRES SANTÉ ---");
        System.out.print("⚖️ Nouveau poids (" + currentUser.getPoids() + " kg) : ");
        Double poids = lireDouble();
        System.out.print("📏 Nouvelle taille (" + currentUser.getTaille() + " m) : ");
        Double taille = lireDouble();
        System.out.print("🩸 Nouvelle glycémie (" + currentUser.getGlycemie() + ") : ");
        Double glycemie = lireDouble();
        System.out.print("💓 Nouvelle tension (" + currentUser.getTension() + ") : ");
        String tension = scanner.nextLine();

        service.updatePatientHealth(currentUser.getId(), poids, taille, glycemie, tension);
        System.out.println("✅ Paramètres santé mis à jour !");
        currentUser = service.getUserById(currentUser.getId());
    }

    private static void recommandationIA() throws SQLException {
        User user = service.getUserById(currentUser.getId());
        String rec = service.getRecommendationIA(user);
        System.out.println("\n🤖 RECOMMANDATION IA : " + rec);
    }

    // ==================== RENDEZ-VOUS PATIENT ====================

    private static void prendreRendezVous() throws SQLException {
        System.out.println("\n--- PRENDRE RENDEZ-VOUS ---");
        List<User> medecins = service.getAllMedecins();

        if (medecins.isEmpty()) {
            System.out.println("❌ Aucun médecin disponible !");
            return;
        }

        System.out.println("👨‍⚕️ Médecins disponibles :");
        for (User m : medecins) {
            System.out.println("   ID: " + m.getId() + " - Dr. " + m.getLastName() + " (" + m.getSpecialite() + ")");
        }

        System.out.print("👉 ID du médecin : ");
        int medecinId = lireEntier();

        System.out.print("📅 Date et heure (format: 2026-04-10 14:30) : ");
        String dateStr = scanner.nextLine();
        LocalDateTime date = LocalDateTime.parse(dateStr, formatter);

        System.out.print("💬 Motif : ");
        String motif = scanner.nextLine();

        Appointment rdv = new Appointment();
        rdv.setPatientId(currentUser.getId());
        rdv.setDoctorId(medecinId);
        rdv.setDate(date);
        rdv.setReason(motif);

        service.prendreRendezVous(rdv);
        System.out.println("✅ Rendez-vous créé avec ID : " + rdv.getId());
    }

    private static void mesRendezVous() throws SQLException {
        System.out.println("\n--- MES RENDEZ-VOUS ---");
        List<Appointment> rdvs = service.getRdvByPatient(currentUser.getId());

        if (rdvs.isEmpty()) {
            System.out.println("📭 Aucun rendez-vous trouvé.");
            return;
        }

        System.out.println("📅 Liste de vos rendez-vous :");
        for (Appointment rdv : rdvs) {
            String statusIcon = rdv.getStatus().equals("en_attente") ? "⏳" :
                    rdv.getStatus().equals("confirme") ? "✅" :
                            rdv.getStatus().equals("annule") ? "❌" : "🏁";
            System.out.println("   " + statusIcon + " ID:" + rdv.getId() + " - Dr. " + rdv.getDoctorNom() +
                    " - " + rdv.getDate().format(formatter) + " - " + rdv.getStatus() +
                    " - Motif: " + rdv.getReason());
        }
    }

    private static void voirTousMedecins() throws SQLException {
        System.out.println("\n--- LISTE DES MÉDECINS ---");
        List<User> medecins = service.getAllMedecins();

        if (medecins.isEmpty()) {
            System.out.println("❌ Aucun médecin inscrit.");
            return;
        }

        for (User m : medecins) {
            System.out.println("   👨‍⚕️ Dr. " + m.getLastName() + " " + m.getFirstName() +
                    " - " + m.getSpecialite() + " (ID: " + m.getId() + ")");
        }
    }

    // ==================== FONCTIONS MÉDECIN ====================

    private static void mesPatients() throws SQLException {
        System.out.println("\n--- MES PATIENTS ---");
        List<Appointment> rdvs = service.getRdvByMedecin(currentUser.getId());

        if (rdvs.isEmpty()) {
            System.out.println("📭 Aucun patient pour le moment.");
            return;
        }

        Set<Integer> patientIds = new HashSet<>();
        System.out.println("👥 Liste des patients qui ont pris rendez-vous avec moi :");
        for (Appointment rdv : rdvs) {
            if (!patientIds.contains(rdv.getPatientId())) {
                patientIds.add(rdv.getPatientId());
                User patient = service.getUserById(rdv.getPatientId());
                System.out.println("   🧑 Patient : " + patient.getFirstName() + " " + patient.getLastName() +
                        " (ID: " + patient.getId() + ") - " + patient.getEmail());
            }
        }
    }

    private static void gererRendezVousMedecin() throws SQLException {
        System.out.println("\n--- GESTION DES RENDEZ-VOUS ---");
        List<Appointment> rdvs = service.getRdvByMedecin(currentUser.getId());

        if (rdvs.isEmpty()) {
            System.out.println("📭 Aucun rendez-vous pour le moment.");
            return;
        }

        System.out.println("📅 Liste des rendez-vous :");
        for (Appointment rdv : rdvs) {
            User patient = service.getUserById(rdv.getPatientId());
            String statusIcon = rdv.getStatus().equals("en_attente") ? "⏳" :
                    rdv.getStatus().equals("confirme") ? "✅" :
                            rdv.getStatus().equals("annule") ? "❌" : "🏁";
            System.out.println("   " + statusIcon + " ID:" + rdv.getId() +
                    " - Patient: " + patient.getFirstName() + " " + patient.getLastName() +
                    " - " + rdv.getDate().format(formatter) +
                    " - Statut: " + rdv.getStatus());
        }

        System.out.print("\n👉 ID du rendez-vous à modifier (0 pour quitter) : ");
        int rdvId = lireEntier();
        if (rdvId == 0) return;

        System.out.println("1. ✅ Confirmer");
        System.out.println("2. ❌ Annuler");
        System.out.println("3. 🏁 Terminer");
        System.out.print("👉 Votre choix : ");
        int choix = lireEntier();

        String newStatus = "";
        switch (choix) {
            case 1: newStatus = "confirme"; break;
            case 2: newStatus = "annule"; break;
            case 3: newStatus = "termine"; break;
            default:
                System.out.println("❌ Choix invalide !");
                return;
        }

        service.updateAppointmentStatus(rdvId, newStatus);
        System.out.println("✅ Statut du rendez-vous mis à jour : " + newStatus);
    }

    private static void menuRechercheMedecin() throws SQLException {
        System.out.println("\n┌───────────── RECHERCHE PATIENT ─────────────┐");
        System.out.println("│ 1. 🔍 Rechercher par nom                     │");
        System.out.println("│ 2. 📧 Rechercher par email                   │");
        System.out.println("│ 0. 🔙 Retour                                 │");
        System.out.println("└──────────────────────────────────────────────┘");
        System.out.print("👉 Votre choix : ");

        int choix = lireEntier();
        switch (choix) {
            case 1: rechercherPatientParNom(); break;
            case 2: rechercherPatientParEmail(); break;
            default: System.out.println("❌ Choix invalide !");
        }
    }

    private static void rechercherPatientParNom() throws SQLException {
        System.out.print("🔍 Nom ou prénom à rechercher : ");
        String nom = scanner.nextLine();
        List<User> users = service.rechercherUtilisateursParNom(nom);

        if (users.isEmpty()) {
            System.out.println("📭 Aucun patient trouvé pour : " + nom);
            return;
        }

        System.out.println("📋 Résultats de la recherche :");
        for (User u : users) {
            if (u.getRole().equals("patient")) {
                System.out.println("   🧑 ID:" + u.getId() + " - " + u.getFirstName() + " " + u.getLastName() +
                        " - " + u.getEmail());
            }
        }
    }

    private static void rechercherPatientParEmail() throws SQLException {
        System.out.print("🔍 Email à rechercher : ");
        String email = scanner.nextLine();
        List<User> users = service.rechercherUtilisateursParEmail(email);

        if (users.isEmpty()) {
            System.out.println("📭 Aucun patient trouvé pour : " + email);
            return;
        }

        System.out.println("📋 Résultats de la recherche :");
        for (User u : users) {
            if (u.getRole().equals("patient")) {
                System.out.println("   🧑 ID:" + u.getId() + " - " + u.getFirstName() + " " + u.getLastName() +
                        " - " + u.getEmail());
            }
        }
    }

    // ==================== FONCTIONS ADMIN ====================

    private static void listerTousUtilisateurs() throws SQLException {
        System.out.println("\n--- TOUS LES UTILISATEURS ---");
        List<User> users = service.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("📭 Aucun utilisateur trouvé.");
            return;
        }

        System.out.println("📋 Liste complète des utilisateurs :");
        for (User u : users) {
            String verifiedIcon = u.isVerified() ? "✅" : "⏳";
            System.out.println("   " + verifiedIcon + " ID:" + u.getId() +
                    " - " + u.getEmail() +
                    " (" + u.getRole() + ")" +
                    " - " + u.getFirstName() + " " + u.getLastName());
        }
    }

    private static void supprimerUtilisateur() throws SQLException {
        System.out.println("\n--- SUPPRESSION D'UN UTILISATEUR ---");
        System.out.print("🆔 ID de l'utilisateur à supprimer : ");
        int id = lireEntier();

        User user = service.getUserById(id);
        if (user == null) {
            System.out.println("❌ Utilisateur non trouvé !");
            return;
        }

        System.out.println("⚠️ Vous allez supprimer : " + user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() + ")");
        System.out.print("Confirmer la suppression ? (o/n) : ");
        String confirm = scanner.nextLine();

        if (confirm.equalsIgnoreCase("o")) {
            service.deleteUser(id);
            System.out.println("✅ Utilisateur supprimé avec succès !");
        } else {
            System.out.println("❌ Suppression annulée.");
        }
    }

    private static void activerDesactiverUtilisateur() throws SQLException {
        System.out.println("\n--- ACTIVER/DÉSACTIVER UN UTILISATEUR ---");
        System.out.print("🆔 ID de l'utilisateur : ");
        int id = lireEntier();

        User user = service.getUserById(id);
        if (user == null) {
            System.out.println("❌ Utilisateur non trouvé !");
            return;
        }

        System.out.println("👤 Utilisateur : " + user.getFirstName() + " " + user.getLastName());
        System.out.println("📧 Email : " + user.getEmail());
        System.out.println("🔓 Statut actuel : " + (user.isVerified() ? "✅ Activé" : "⏳ Désactivé"));

        System.out.print("Voulez-vous changer le statut ? (o/n) : ");
        String confirm = scanner.nextLine();

        if (confirm.equalsIgnoreCase("o")) {
            user.setVerified(!user.isVerified());
            service.updateUser(user);
            System.out.println("✅ Statut mis à jour : " + (user.isVerified() ? "Activé" : "Désactivé"));
        } else {
            System.out.println("❌ Opération annulée.");
        }
    }

    private static void menuRechercheAdmin() throws SQLException {
        System.out.println("\n┌───────────── RECHERCHE UTILISATEURS ─────────────┐");
        System.out.println("│ 1. 🔍 Rechercher par nom                         │");
        System.out.println("│ 2. 📧 Rechercher par email                       │");
        System.out.println("│ 3. 👑 Rechercher par rôle (patient/médecin/admin)│");
        System.out.println("│ 4. 🏥 Rechercher médecin par spécialité          │");
        System.out.println("│ 0. 🔙 Retour                                     │");
        System.out.println("└──────────────────────────────────────────────────┘");
        System.out.print("👉 Votre choix : ");

        int choix = lireEntier();
        switch (choix) {
            case 1: rechercherUtilisateurParNom(); break;
            case 2: rechercherUtilisateurParEmail(); break;
            case 3: rechercherUtilisateurParRole(); break;
            case 4: rechercherMedecinParSpecialite(); break;
            default: System.out.println("❌ Choix invalide !");
        }
    }

    private static void rechercherUtilisateurParNom() throws SQLException {
        System.out.print("🔍 Nom ou prénom à rechercher : ");
        String nom = scanner.nextLine();
        List<User> users = service.rechercherUtilisateursParNom(nom);

        if (users.isEmpty()) {
            System.out.println("📭 Aucun utilisateur trouvé pour : " + nom);
            return;
        }

        System.out.println("📋 Résultats de la recherche :");
        for (User u : users) {
            String verifiedIcon = u.isVerified() ? "✅" : "⏳";
            System.out.println("   " + verifiedIcon + " ID:" + u.getId() + " - " + u.getFirstName() + " " + u.getLastName() +
                    " (" + u.getRole() + ") - " + u.getEmail());
        }
    }

    private static void rechercherUtilisateurParEmail() throws SQLException {
        System.out.print("🔍 Email à rechercher : ");
        String email = scanner.nextLine();
        List<User> users = service.rechercherUtilisateursParEmail(email);

        if (users.isEmpty()) {
            System.out.println("📭 Aucun utilisateur trouvé pour : " + email);
            return;
        }

        System.out.println("📋 Résultats de la recherche :");
        for (User u : users) {
            String verifiedIcon = u.isVerified() ? "✅" : "⏳";
            System.out.println("   " + verifiedIcon + " ID:" + u.getId() + " - " + u.getFirstName() + " " + u.getLastName() +
                    " (" + u.getRole() + ") - " + u.getEmail());
        }
    }

    private static void rechercherUtilisateurParRole() throws SQLException {
        System.out.println("Rôles disponibles : patient, medecin, admin");
        System.out.print("🔍 Rôle à rechercher : ");
        String role = scanner.nextLine().toLowerCase();
        List<User> users = service.rechercherUtilisateursParRole(role);

        if (users.isEmpty()) {
            System.out.println("📭 Aucun utilisateur trouvé avec le rôle : " + role);
            return;
        }

        System.out.println("📋 Résultats de la recherche :");
        for (User u : users) {
            String verifiedIcon = u.isVerified() ? "✅" : "⏳";
            System.out.println("   " + verifiedIcon + " ID:" + u.getId() + " - " + u.getFirstName() + " " + u.getLastName() +
                    " - " + u.getEmail());
        }
    }

    private static void rechercherMedecinParSpecialite() throws SQLException {
        System.out.print("🔍 Spécialité à rechercher : ");
        String specialite = scanner.nextLine();
        List<User> medecins = service.rechercherMedecinsParSpecialite(specialite);

        if (medecins.isEmpty()) {
            System.out.println("📭 Aucun médecin trouvé avec la spécialité : " + specialite);
            return;
        }

        System.out.println("📋 Résultats de la recherche :");
        for (User m : medecins) {
            System.out.println("   👨‍⚕️ ID:" + m.getId() + " - Dr. " + m.getLastName() + " " + m.getFirstName() +
                    " - " + m.getSpecialite() + " - " + m.getEmail());
        }
    }

    // ==================== UTILITAIRES ====================

    private static int lireEntier() {
        while (!scanner.hasNextInt()) {
            System.out.print("❌ Veuillez entrer un nombre : ");
            scanner.next();
        }
        int valeur = scanner.nextInt();
        scanner.nextLine();
        return valeur;
    }

    private static Double lireDouble() {
        while (!scanner.hasNextDouble()) {
            System.out.print("❌ Veuillez entrer un nombre (ex: 70.5) : ");
            scanner.next();
        }
        double valeur = scanner.nextDouble();
        scanner.nextLine();
        return valeur;
    }
}