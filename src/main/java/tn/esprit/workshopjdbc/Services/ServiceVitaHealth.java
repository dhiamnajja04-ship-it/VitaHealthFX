package tn.esprit.workshopjdbc.Services;

import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.dao.UserDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ServiceVitaHealth {
    private UserDAO userDAO = new UserDAO();

    // ==================== AUTHENTIFICATION ====================
    
    public User login(String email, String password) throws SQLException {
        try {
            User user = userDAO.findByEmail(email);
            if (user != null && user.getPassword() != null) {
                String storedPassword = user.getPassword();
                
                if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                    try {
                        if (BCrypt.checkpw(password, storedPassword)) {
                            System.out.println("✅ Connexion réussie: " + email);
                            return user;
                        }
                    } catch (IllegalArgumentException e) {
                        System.err.println("Erreur BCrypt: " + e.getMessage());
                        if (storedPassword.equals(password)) {
                            System.out.println("✅ Connexion réussie (fallback): " + email);
                            return user;
                        }
                    }
                } else {
                    if (storedPassword.equals(password)) {
                        System.out.println("✅ Connexion réussie (texte clair): " + email);
                        migrateToBCrypt(user, password);
                        return user;
                    }
                }
            }
            System.out.println("❌ Échec connexion: " + email);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void migrateToBCrypt(User user, String plainPassword) {
        try {
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
            user.setPassword(hashedPassword);
            userDAO.update(user);
            System.out.println("✅ Mot de passe migré vers BCrypt pour: " + user.getEmail());
        } catch (SQLException e) {
            System.err.println("❌ Échec migration BCrypt: " + e.getMessage());
        }
    }
    
    public boolean register(User user, String plainPassword) throws SQLException {
        if (userDAO.emailExists(user.getEmail())) {
            System.err.println("❌ Email déjà existant: " + user.getEmail());
            return false;
        }
        
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        user.setPassword(hashedPassword);
        
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("PATIENT");
        }
        
        user.setVerified(false);
        
        return userDAO.ajouter(user);
    }
    
    public void createDefaultAdmin() {
        try {
            User existingAdmin = userDAO.findByEmail("admin11@vitahealth.com");
            
            if (existingAdmin == null) {
                User admin = new User();
                admin.setEmail("admin11@vitahealth.com");
                admin.setFirstName("Admin");
                admin.setLastName("System");
                admin.setRole("ADMIN");
                admin.setVerified(true);
                
                String hashedPassword = BCrypt.hashpw("admin123", BCrypt.gensalt());
                admin.setPassword(hashedPassword);
                
                boolean success = userDAO.ajouter(admin);
                
                if (success) {
                    System.out.println("✅ Compte ADMIN créé avec succès!");
                    System.out.println("   Email: admin11@vitahealth.com");
                    System.out.println("   Mot de passe: admin123");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur création admin: " + e.getMessage());
        }
    }
    
    // ==================== MOT DE PASSE OUBLIÉ (AMÉLIORÉ) ====================
    
    public User getUserByEmail(String email) throws SQLException {
        return userDAO.findByEmail(email);
    }
    
    public boolean resetPassword(int userId, String newPassword) throws SQLException {
        User user = userDAO.findById(userId);
        if (user != null) {
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            user.setPassword(hashedPassword);
            userDAO.update(user);
            System.out.println("✅ Mot de passe réinitialisé pour: " + user.getEmail());
            return true;
        }
        return false;
    }
    
    public boolean resetPasswordByEmail(String email, String newPassword) throws SQLException {
        User user = userDAO.findByEmail(email);
        if (user != null) {
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            user.setPassword(hashedPassword);
            userDAO.update(user);
            System.out.println("✅ Mot de passe réinitialisé pour: " + email);
            return true;
        }
        return false;
    }
    
    public String generateVerificationCode() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));
    }
    
    public void sendVerificationEmail(String email, String code) {
        System.out.println("📧 === ENVOI D'EMAIL DE VÉRIFICATION ===");
        System.out.println("   À: " + email);
        System.out.println("   Sujet: Code de réinitialisation - VitaHealth");
        System.out.println("   Message: Votre code de vérification est: " + code);
        System.out.println("   Ce code est valable 15 minutes.");
        System.out.println("=========================================");
    }

    // ==================== CRUD ====================
    
    public User getUserById(int id) throws SQLException {
        return userDAO.findById(id);
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }

    public List<User> getAllMedecins() throws SQLException {
        return userDAO.getAllMedecins();
    }
    
    public List<User> getAllPatients() throws SQLException {
        return userDAO.findByRole("PATIENT");
    }
    
    public List<User> getAllAdmins() throws SQLException {
        return userDAO.findByRole("ADMIN");
    }

    public void updateUser(User user) throws SQLException {
        userDAO.update(user);
    }
    
    public boolean updateUserRole(int userId, String newRole) throws SQLException {
        return userDAO.updateUserRole(userId, newRole);
    }

    public void deleteUser(int id) throws SQLException {
        userDAO.delete(id);
    }

    // ==================== RECHERCHE ====================
    
    public List<User> rechercherUtilisateursParNom(String nom) throws SQLException {
        return userDAO.rechercherParNom(nom);
    }

    public List<User> rechercherUtilisateursParEmail(String email) throws SQLException {
        return userDAO.rechercherParEmail(email);
    }

    public List<User> rechercherUtilisateursParRole(String role) throws SQLException {
        return userDAO.rechercherParRole(role);
    }
    
    public List<User> rechercherUtilisateursParCin(String cin) throws SQLException {
        return userDAO.findAll().stream()
                .filter(u -> u.getCin() != null && u.getCin().toLowerCase().contains(cin.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    public List<User> rechercherUtilisateursParTelephone(String phone) throws SQLException {
        return userDAO.findAll().stream()
                .filter(u -> u.getPhone() != null && u.getPhone().contains(phone))
                .collect(Collectors.toList());
    }

    // ==================== STATISTIQUES ====================
    
    public long compterUtilisateursParRole(String role) throws SQLException {
        return userDAO.compterParRole(role);
    }
    
    public long compterTotalUtilisateurs() throws SQLException {
        return userDAO.countAllUsers();
    }
    
    public long compterUtilisateursVerifies() throws SQLException {
        return userDAO.findAll().stream().filter(User::isVerified).count();
    }
    
    public long compterUtilisateursNonVerifies() throws SQLException {
        return userDAO.findAll().stream().filter(u -> !u.isVerified()).count();
    }

    public double moyennePoidsPatients() throws SQLException {
        return userDAO.moyennePoidsPatients();
    }
    
    public double moyenneTaillePatients() throws SQLException {
        return userDAO.findAll().stream()
                .filter(u -> "PATIENT".equals(u.getRole()))
                .filter(u -> u.getTaille() != null)
                .mapToDouble(User::getTaille)
                .average()
                .orElse(0.0);
    }
    
    public double moyenneGlycemiePatients() throws SQLException {
        return userDAO.findAll().stream()
                .filter(u -> "PATIENT".equals(u.getRole()))
                .filter(u -> u.getGlycemie() != null)
                .mapToDouble(User::getGlycemie)
                .average()
                .orElse(0.0);
    }
    
    // ==================== STATISTIQUES AVANCÉES (NOUVEAU) ====================
    
    public long getPatientCount() throws SQLException {
        return compterUtilisateursParRole("PATIENT");
    }
    
    public long getDoctorCount() throws SQLException {
        return compterUtilisateursParRole("DOCTOR");
    }
    
    public long getAdminCount() throws SQLException {
        return compterUtilisateursParRole("ADMIN");
    }
    
    public double getAverageBMI() throws SQLException {
        List<User> patients = getAllPatients();
        return patients.stream()
                .filter(p -> p.getPoids() != null && p.getTaille() != null && p.getTaille() > 0)
                .mapToDouble(p -> p.getPoids() / (p.getTaille() * p.getTaille()))
                .average()
                .orElse(0.0);
    }
    
    public long getOverweightCount() throws SQLException {
        List<User> patients = getAllPatients();
        return patients.stream()
                .filter(p -> p.getPoids() != null && p.getTaille() != null && p.getTaille() > 0)
                .filter(p -> (p.getPoids() / (p.getTaille() * p.getTaille())) > 25)
                .count();
    }
    
    public long getHighGlycemiaCount() throws SQLException {
        List<User> patients = getAllPatients();
        return patients.stream()
                .filter(p -> p.getGlycemie() != null && p.getGlycemie() > 1.26)
                .count();
    }

    // ==================== VÉRIFICATION ====================
    
    public boolean emailExiste(String email) throws SQLException {
        return userDAO.emailExists(email);
    }
    
    public boolean verifierUtilisateur(int userId) throws SQLException {
        User user = userDAO.findById(userId);
        if (user != null) {
            user.setVerified(true);
            userDAO.update(user);
            return true;
        }
        return false;
    }
}