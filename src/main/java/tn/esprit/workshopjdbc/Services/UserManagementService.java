package tn.esprit.workshopjdbc.Services;

import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.dao.UserDAO;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de gestion des utilisateurs avec Stream API
 * Ce service utilise le UserDAO existant sans le modifier
 */
public class UserManagementService {
    
    private UserDAO userDAO;
    
    public UserManagementService() {
        this.userDAO = new UserDAO();
    }
    
    // ==================== MÉTHODES CRUD ====================
    
    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }
    
    public User getUserById(int id) throws SQLException {
        return userDAO.findById(id);
    }
    
    public void deleteUser(int id) throws SQLException {
        userDAO.delete(id);
    }
    
    // ==================== GESTION DES RÔLES ====================
    
    public void promoteToDoctor(int userId) throws SQLException {
        userDAO.updateUserRole(userId, "DOCTOR");
    }
    
    public void promoteToAdmin(int userId) throws SQLException {
        userDAO.updateUserRole(userId, "ADMIN");
    }
    
    public void demoteToPatient(int userId) throws SQLException {
        userDAO.updateUserRole(userId, "PATIENT");
    }
    
    // ==================== RECHERCHE AVEC STREAM ====================
    
    public List<User> searchByNom(String keyword) throws SQLException {
        if (keyword == null || keyword.isEmpty()) {
            return userDAO.findAll();
        }
        return userDAO.findAll().stream()
                .filter(u -> u.getFirstName() != null && u.getFirstName().toLowerCase().contains(keyword.toLowerCase()) ||
                             u.getLastName() != null && u.getLastName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    public List<User> searchByEmail(String email) throws SQLException {
        if (email == null || email.isEmpty()) {
            return userDAO.findAll();
        }
        return userDAO.findAll().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().toLowerCase().contains(email.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    public List<User> searchByRole(String role) throws SQLException {
        if (role == null || role.isEmpty()) {
            return userDAO.findAll();
        }
        return userDAO.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().equalsIgnoreCase(role))
                .collect(Collectors.toList());
    }
    
    public List<User> searchByNomAndRole(String keyword, String role) throws SQLException {
        List<User> users = userDAO.findAll();
        
        if (keyword != null && !keyword.isEmpty()) {
            users = users.stream()
                    .filter(u -> (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(keyword.toLowerCase())) ||
                                 (u.getLastName() != null && u.getLastName().toLowerCase().contains(keyword.toLowerCase())))
                    .collect(Collectors.toList());
        }
        
        if (role != null && !role.isEmpty() && !"Tous".equals(role)) {
            users = users.stream()
                    .filter(u -> u.getRole() != null && u.getRole().equalsIgnoreCase(role))
                    .collect(Collectors.toList());
        }
        
        return users;
    }
    
    public List<User> searchByPhone(String phone) throws SQLException {
        if (phone == null || phone.isEmpty()) {
            return userDAO.findAll();
        }
        return userDAO.findAll().stream()
                .filter(u -> u.getPhone() != null && u.getPhone().contains(phone))
                .collect(Collectors.toList());
    }
    
    public List<User> searchByCin(String cin) throws SQLException {
        if (cin == null || cin.isEmpty()) {
            return userDAO.findAll();
        }
        return userDAO.findAll().stream()
                .filter(u -> u.getCin() != null && u.getCin().contains(cin))
                .collect(Collectors.toList());
    }
    
    public List<User> searchBySpecialite(String specialite) throws SQLException {
        if (specialite == null || specialite.isEmpty()) {
            return userDAO.findAll();
        }
        return userDAO.findAll().stream()
                .filter(u -> u.getSpecialite() != null && u.getSpecialite().toLowerCase().contains(specialite.toLowerCase()))
                .filter(u -> "DOCTOR".equals(u.getRole()))
                .collect(Collectors.toList());
    }
    
    public List<User> getUnverifiedUsers() throws SQLException {
        return userDAO.findAll().stream()
                .filter(u -> !u.isVerified())
                .collect(Collectors.toList());
    }
    
    public List<User> getVerifiedUsers() throws SQLException {
        return userDAO.findAll().stream()
                .filter(User::isVerified)
                .collect(Collectors.toList());
    }
    
    // ==================== TRI AVEC STREAM ====================
    
    public List<User> sortById(boolean ascending) throws SQLException {
        if (ascending) {
            return userDAO.findAll().stream()
                    .sorted(Comparator.comparingInt(User::getId))
                    .collect(Collectors.toList());
        } else {
            return userDAO.findAll().stream()
                    .sorted(Comparator.comparingInt(User::getId).reversed())
                    .collect(Collectors.toList());
        }
    }
    
    public List<User> sortByFirstName(boolean ascending) throws SQLException {
        if (ascending) {
            return userDAO.findAll().stream()
                    .sorted(Comparator.comparing(User::getFirstName, Comparator.nullsLast(String::compareTo)))
                    .collect(Collectors.toList());
        } else {
            return userDAO.findAll().stream()
                    .sorted(Comparator.comparing(User::getFirstName, Comparator.nullsLast(String::compareTo)).reversed())
                    .collect(Collectors.toList());
        }
    }
    
    public List<User> sortByEmail(boolean ascending) throws SQLException {
        if (ascending) {
            return userDAO.findAll().stream()
                    .sorted(Comparator.comparing(User::getEmail, Comparator.nullsLast(String::compareTo)))
                    .collect(Collectors.toList());
        } else {
            return userDAO.findAll().stream()
                    .sorted(Comparator.comparing(User::getEmail, Comparator.nullsLast(String::compareTo)).reversed())
                    .collect(Collectors.toList());
        }
    }
    
    public List<User> sortByRole() throws SQLException {
        return userDAO.findAll().stream()
                .sorted(Comparator.comparing(User::getRole, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }
    
    public List<User> sortByRoleThenName() throws SQLException {
        return userDAO.findAll().stream()
                .sorted(Comparator.comparing(User::getRole, Comparator.nullsLast(String::compareTo))
                        .thenComparing(User::getFirstName, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }
    
    // ==================== RECHERCHE + TRI COMBINÉS ====================
    
    public List<User> searchAndSort(String keyword, String role, String sortField, String sortOrder) throws SQLException {
        List<User> users = userDAO.findAll();
        
        if (keyword != null && !keyword.isEmpty()) {
            users = users.stream()
                    .filter(u -> (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(keyword.toLowerCase())) ||
                                 (u.getLastName() != null && u.getLastName().toLowerCase().contains(keyword.toLowerCase())) ||
                                 (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword.toLowerCase())))
                    .collect(Collectors.toList());
        }
        
        if (role != null && !role.isEmpty() && !"Tous".equals(role)) {
            users = users.stream()
                    .filter(u -> u.getRole() != null && u.getRole().equalsIgnoreCase(role))
                    .collect(Collectors.toList());
        }
        
        users = applySorting(users, sortField, sortOrder);
        
        return users;
    }
    
    private List<User> applySorting(List<User> users, String sortField, String sortOrder) {
        boolean ascending = "asc".equalsIgnoreCase(sortOrder);
        
        switch (sortField) {
            case "id":
                if (ascending) {
                    users.sort(Comparator.comparingInt(User::getId));
                } else {
                    users.sort(Comparator.comparingInt(User::getId).reversed());
                }
                break;
            case "firstName":
                if (ascending) {
                    users.sort(Comparator.comparing(User::getFirstName, Comparator.nullsLast(String::compareTo)));
                } else {
                    users.sort(Comparator.comparing(User::getFirstName, Comparator.nullsLast(String::compareTo)).reversed());
                }
                break;
            case "email":
                if (ascending) {
                    users.sort(Comparator.comparing(User::getEmail, Comparator.nullsLast(String::compareTo)));
                } else {
                    users.sort(Comparator.comparing(User::getEmail, Comparator.nullsLast(String::compareTo)).reversed());
                }
                break;
            case "role":
                users.sort(Comparator.comparing(User::getRole, Comparator.nullsLast(String::compareTo)));
                break;
            default:
                users.sort(Comparator.comparingInt(User::getId).reversed());
        }
        return users;
    }
    
    // ==================== STATISTIQUES AVEC STREAM ====================
    
    public long countByRole(String role) throws SQLException {
        return userDAO.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().equalsIgnoreCase(role))
                .count();
    }
    
    public UserStats getStats() throws SQLException {
        List<User> users = userDAO.findAll();
        
        long total = users.size();
        long patients = users.stream().filter(u -> "PATIENT".equals(u.getRole())).count();
        long doctors = users.stream().filter(u -> "DOCTOR".equals(u.getRole())).count();
        long admins = users.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
        long verified = users.stream().filter(User::isVerified).count();
        long unverified = total - verified;
        
        double avgPoids = users.stream()
                .filter(u -> "PATIENT".equals(u.getRole()))
                .filter(u -> u.getPoids() != null)
                .mapToDouble(User::getPoids)
                .average()
                .orElse(0.0);
        
        double avgTaille = users.stream()
                .filter(u -> "PATIENT".equals(u.getRole()))
                .filter(u -> u.getTaille() != null)
                .mapToDouble(User::getTaille)
                .average()
                .orElse(0.0);
        
        double avgGlycemie = users.stream()
                .filter(u -> "PATIENT".equals(u.getRole()))
                .filter(u -> u.getGlycemie() != null)
                .mapToDouble(User::getGlycemie)
                .average()
                .orElse(0.0);
        
        return new UserStats(total, patients, doctors, admins, verified, unverified, avgPoids, avgTaille, avgGlycemie);
    }
    
    /**
     * Statistiques complètes avancées
     */
    public AdvancedStats getAdvancedStats() throws SQLException {
        List<User> users = userDAO.findAll();
        List<User> patients = users.stream().filter(u -> "PATIENT".equals(u.getRole())).collect(Collectors.toList());
        
        // IMC moyen
        double avgBMI = patients.stream()
                .filter(p -> p.getPoids() != null && p.getTaille() != null && p.getTaille() > 0)
                .mapToDouble(p -> p.getPoids() / (p.getTaille() * p.getTaille()))
                .average()
                .orElse(0.0);
        
        // Catégories IMC
        long underweight = patients.stream()
                .filter(p -> p.getPoids() != null && p.getTaille() != null && p.getTaille() > 0)
                .filter(p -> (p.getPoids() / (p.getTaille() * p.getTaille())) < 18.5)
                .count();
        
        long normal = patients.stream()
                .filter(p -> p.getPoids() != null && p.getTaille() != null && p.getTaille() > 0)
                .filter(p -> {
                    double bmi = p.getPoids() / (p.getTaille() * p.getTaille());
                    return bmi >= 18.5 && bmi < 25;
                })
                .count();
        
        long overweight = patients.stream()
                .filter(p -> p.getPoids() != null && p.getTaille() != null && p.getTaille() > 0)
                .filter(p -> {
                    double bmi = p.getPoids() / (p.getTaille() * p.getTaille());
                    return bmi >= 25 && bmi < 30;
                })
                .count();
        
        long obese = patients.stream()
                .filter(p -> p.getPoids() != null && p.getTaille() != null && p.getTaille() > 0)
                .filter(p -> {
                    double bmi = p.getPoids() / (p.getTaille() * p.getTaille());
                    return bmi >= 30;
                })
                .count();
        
        // Glycémie
        long highGlycemia = patients.stream()
                .filter(p -> p.getGlycemie() != null && p.getGlycemie() > 1.26)
                .count();
        
        long lowGlycemia = patients.stream()
                .filter(p -> p.getGlycemie() != null && p.getGlycemie() < 0.70)
                .count();
        
        return new AdvancedStats(avgBMI, underweight, normal, overweight, obese, highGlycemia, lowGlycemia);
    }
    
    public List<String> getAllSpecialites() throws SQLException {
        return userDAO.findAll().stream()
                .filter(u -> "DOCTOR".equals(u.getRole()))
                .filter(u -> u.getSpecialite() != null && !u.getSpecialite().isEmpty())
                .map(User::getSpecialite)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    public List<User> getRecentUsers(int limit) throws SQLException {
        return userDAO.findAll().stream()
                .sorted(Comparator.comparingInt(User::getId).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    // ==================== API MÉTIER AVANCÉES ====================
    
    /**
     * Récupérer tous les médecins avec leurs patients
     */
    public Map<String, List<User>> getDoctorsWithPatients() throws SQLException {
        List<User> doctors = userDAO.findByRole("DOCTOR");
        List<User> patients = userDAO.findByRole("PATIENT");
        
        return doctors.stream()
                .collect(Collectors.toMap(
                    d -> d.getFirstName() + " " + d.getLastName(),
                    d -> patients
                ));
    }
    
    /**
     * Patients ayant une glycémie élevée (> 1.26 g/L)
     */
    public List<User> getPatientsWithHighGlycemia() throws SQLException {
        return userDAO.findAll().stream()
                .filter(u -> "PATIENT".equals(u.getRole()))
                .filter(u -> u.getGlycemie() != null && u.getGlycemie() > 1.26)
                .collect(Collectors.toList());
    }
    
    /**
     * Patients en surpoids (IMC > 25)
     */
    public List<User> getOverweightPatients() throws SQLException {
        return userDAO.findAll().stream()
                .filter(u -> "PATIENT".equals(u.getRole()))
                .filter(u -> u.getPoids() != null && u.getTaille() != null && u.getTaille() > 0)
                .filter(u -> {
                    double imc = u.getPoids() / (u.getTaille() * u.getTaille());
                    return imc > 25;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Patients obèses (IMC > 30)
     */
    public List<User> getObesePatients() throws SQLException {
        return userDAO.findAll().stream()
                .filter(u -> "PATIENT".equals(u.getRole()))
                .filter(u -> u.getPoids() != null && u.getTaille() != null && u.getTaille() > 0)
                .filter(u -> {
                    double imc = u.getPoids() / (u.getTaille() * u.getTaille());
                    return imc > 30;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Calculer l'IMC d'un patient
     */
    public double calculateBMI(User patient) {
        if (patient == null || patient.getPoids() == null || patient.getTaille() == null || patient.getTaille() == 0) {
            return 0;
        }
        return patient.getPoids() / (patient.getTaille() * patient.getTaille());
    }
    
    /**
     * Obtenir la répartition par rôle (pour graphiques)
     */
    public Map<String, Long> getRoleDistribution() throws SQLException {
        return userDAO.findAll().stream()
                .collect(Collectors.groupingBy(
                    u -> u.getRole() != null ? u.getRole() : "UNKNOWN",
                    Collectors.counting()
                ));
    }
    
    /**
     * Taux de vérification des comptes
     */
    public double getVerificationRate() throws SQLException {
        long total = userDAO.findAll().size();
        if (total == 0) return 0;
        long verified = userDAO.findAll().stream().filter(User::isVerified).count();
        return (double) verified / total * 100;
    }
    
    /**
     * Médecins par spécialité (groupé)
     */
    public Map<String, List<User>> getDoctorsGroupedBySpecialite() throws SQLException {
        return userDAO.findAll().stream()
                .filter(u -> "DOCTOR".equals(u.getRole()))
                .filter(u -> u.getSpecialite() != null && !u.getSpecialite().isEmpty())
                .collect(Collectors.groupingBy(User::getSpecialite));
    }
    
    /**
     * Nombre de médecins par spécialité
     */
    public Map<String, Long> countDoctorsBySpecialite() throws SQLException {
        return userDAO.findAll().stream()
                .filter(u -> "DOCTOR".equals(u.getRole()))
                .filter(u -> u.getSpecialite() != null && !u.getSpecialite().isEmpty())
                .collect(Collectors.groupingBy(User::getSpecialite, Collectors.counting()));
    }
    
    /**
     * Patients avec paramètres médicaux complets
     */
    public List<User> getPatientsWithCompleteMedicalData() throws SQLException {
        return userDAO.findAll().stream()
                .filter(u -> "PATIENT".equals(u.getRole()))
                .filter(u -> u.getPoids() != null && u.getTaille() != null && 
                             u.getGlycemie() != null && u.getTension() != null)
                .collect(Collectors.toList());
    }
    
    /**
     * Recherche multicritères avancée
     */
    public List<User> advancedSearch(String keyword, String role, String specialite, Boolean verified, String sortBy) throws SQLException {
        List<User> users = userDAO.findAll();
        
        if (keyword != null && !keyword.isEmpty()) {
            String k = keyword.toLowerCase();
            users = users.stream()
                    .filter(u -> (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(k)) ||
                                 (u.getLastName() != null && u.getLastName().toLowerCase().contains(k)) ||
                                 (u.getEmail() != null && u.getEmail().toLowerCase().contains(k)))
                    .collect(Collectors.toList());
        }
        
        if (role != null && !role.isEmpty()) {
            users = users.stream()
                    .filter(u -> u.getRole() != null && u.getRole().equalsIgnoreCase(role))
                    .collect(Collectors.toList());
        }
        
        if (specialite != null && !specialite.isEmpty()) {
            users = users.stream()
                    .filter(u -> u.getSpecialite() != null && u.getSpecialite().equalsIgnoreCase(specialite))
                    .collect(Collectors.toList());
        }
        
        if (verified != null) {
            users = users.stream()
                    .filter(u -> u.isVerified() == verified)
                    .collect(Collectors.toList());
        }
        
        if (sortBy != null) {
            switch (sortBy) {
                case "name":
                    users.sort(Comparator.comparing(User::getFirstName, Comparator.nullsLast(String::compareTo)));
                    break;
                case "name_desc":
                    users.sort(Comparator.comparing(User::getFirstName, Comparator.nullsLast(String::compareTo)).reversed());
                    break;
                case "email":
                    users.sort(Comparator.comparing(User::getEmail, Comparator.nullsLast(String::compareTo)));
                    break;
                case "role":
                    users.sort(Comparator.comparing(User::getRole, Comparator.nullsLast(String::compareTo)));
                    break;
                default:
                    users.sort(Comparator.comparingInt(User::getId).reversed());
            }
        }
        
        return users;
    }
    
    /**
     * Export CSV des utilisateurs
     */
    public String exportToCSV() throws SQLException {
        List<User> users = userDAO.findAll();
        StringBuilder sb = new StringBuilder();
        
        sb.append("ID,Prénom,Nom,Email,Rôle,Vérifié,Téléphone,CIN,Spécialité,Maladie,Poids(kg),Taille(m),Glycémie(g/L),Tension\n");
        
        for (User u : users) {
            sb.append(u.getId()).append(",");
            sb.append(escapeCSV(u.getFirstName())).append(",");
            sb.append(escapeCSV(u.getLastName())).append(",");
            sb.append(escapeCSV(u.getEmail())).append(",");
            sb.append(u.getRole()).append(",");
            sb.append(u.isVerified() ? "Oui" : "Non").append(",");
            sb.append(escapeCSV(u.getPhone())).append(",");
            sb.append(escapeCSV(u.getCin())).append(",");
            sb.append(escapeCSV(u.getSpecialite())).append(",");
            sb.append(escapeCSV(u.getMaladie())).append(",");
            sb.append(u.getPoids() != null ? u.getPoids() : "").append(",");
            sb.append(u.getTaille() != null ? u.getTaille() : "").append(",");
            sb.append(u.getGlycemie() != null ? u.getGlycemie() : "").append(",");
            sb.append(escapeCSV(u.getTension())).append("\n");
        }
        
        return sb.toString();
    }
    
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    /**
     * Croissance mensuelle des utilisateurs (12 derniers mois)
     */
    public Map<String, Long> getMonthlyGrowth() throws SQLException {
        Map<String, Long> growth = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 11; i >= 0; i--) {
            LocalDateTime start = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0);
            LocalDateTime end = start.plusMonths(1);
            String monthName = start.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            
            long count = userDAO.findAll().stream()
                    .filter(u -> u.getCreatedAt() != null)
                    .filter(u -> !u.getCreatedAt().isBefore(start) && u.getCreatedAt().isBefore(end))
                    .count();
            growth.put(monthName, count);
        }
        return growth;
    }
    
    /**
     * Patients à risque (nécessitant une attention médicale)
     */
    public List<MedicalAlert> getMedicalAlerts() throws SQLException {
        List<User> patients = userDAO.findByRole("PATIENT");
        List<MedicalAlert> alerts = new ArrayList<>();
        
        for (User p : patients) {
            List<String> reasons = new ArrayList<>();
            
            if (p.getGlycemie() != null && p.getGlycemie() > 1.26) {
                reasons.add("⚠️ Glycémie élevée: " + p.getGlycemie() + " g/L");
            }
            
            if (p.getGlycemie() != null && p.getGlycemie() < 0.70) {
                reasons.add("⚠️ Glycémie basse: " + p.getGlycemie() + " g/L");
            }
            
            if (p.getPoids() != null && p.getTaille() != null && p.getTaille() > 0) {
                double bmi = p.getPoids() / (p.getTaille() * p.getTaille());
                if (bmi > 30) {
                    reasons.add("⚠️ Obésité - IMC: " + String.format("%.1f", bmi));
                }
            }
            
            if (!reasons.isEmpty()) {
                alerts.add(new MedicalAlert(p, reasons));
            }
        }
        
        return alerts;
    }
    
    /**
     * Mise à jour massive des rôles
     */
    public int bulkUpdateRoles(List<Integer> userIds, String newRole) throws SQLException {
        int updated = 0;
        for (int userId : userIds) {
            if (userDAO.updateUserRole(userId, newRole)) {
                updated++;
            }
        }
        return updated;
    }
    
    /**
     * Supprimer les utilisateurs inactifs
     */
    public int deleteInactiveUsers(int daysInactive) throws SQLException {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysInactive);
        List<User> users = userDAO.findAll();
        
        List<User> toDelete = users.stream()
                .filter(u -> u.getUpdatedAt() != null)
                .filter(u -> u.getUpdatedAt().isBefore(cutoff))
                .filter(u -> !"ADMIN".equals(u.getRole()))
                .collect(Collectors.toList());
        
        int deleted = 0;
        for (User u : toDelete) {
            userDAO.delete(u.getId());
            deleted++;
        }
        return deleted;
    }
    
    // ==================== CLASSES INTERNES ====================
    
    public static class UserStats {
        private final long total;
        private final long patients;
        private final long doctors;
        private final long admins;
        private final long verified;
        private final long unverified;
        private final double avgPoids;
        private final double avgTaille;
        private final double avgGlycemie;
        
        public UserStats(long total, long patients, long doctors, long admins, 
                         long verified, long unverified, double avgPoids, double avgTaille, double avgGlycemie) {
            this.total = total;
            this.patients = patients;
            this.doctors = doctors;
            this.admins = admins;
            this.verified = verified;
            this.unverified = unverified;
            this.avgPoids = avgPoids;
            this.avgTaille = avgTaille;
            this.avgGlycemie = avgGlycemie;
        }
        
        public long getTotal() { return total; }
        public long getPatients() { return patients; }
        public long getDoctors() { return doctors; }
        public long getAdmins() { return admins; }
        public long getVerified() { return verified; }
        public long getUnverified() { return unverified; }
        public double getAvgPoids() { return avgPoids; }
        public double getAvgTaille() { return avgTaille; }
        public double getAvgGlycemie() { return avgGlycemie; }
    }
    
    public static class AdvancedStats {
        private final double avgBMI;
        private final long underweight;
        private final long normal;
        private final long overweight;
        private final long obese;
        private final long highGlycemia;
        private final long lowGlycemia;
        
        public AdvancedStats(double avgBMI, long underweight, long normal, long overweight, long obese, long highGlycemia, long lowGlycemia) {
            this.avgBMI = avgBMI;
            this.underweight = underweight;
            this.normal = normal;
            this.overweight = overweight;
            this.obese = obese;
            this.highGlycemia = highGlycemia;
            this.lowGlycemia = lowGlycemia;
        }
        
        public double getAvgBMI() { return avgBMI; }
        public long getUnderweight() { return underweight; }
        public long getNormal() { return normal; }
        public long getOverweight() { return overweight; }
        public long getObese() { return obese; }
        public long getHighGlycemia() { return highGlycemia; }
        public long getLowGlycemia() { return lowGlycemia; }
    }
    
    public static class MedicalAlert {
        private final User user;
        private final List<String> reasons;
        
        public MedicalAlert(User user, List<String> reasons) {
            this.user = user;
            this.reasons = reasons;
        }
        
        public User getUser() { return user; }
        public List<String> getReasons() { return reasons; }
    }
}