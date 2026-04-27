package tn.esprit.workshopjdbc.Services;

import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.dao.UserDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;

public class ServiceVitaHealth {
    private UserDAO userDAO = new UserDAO();

    // ✅ Connexion avec vérification BCrypt
    public User login(String email, String password) throws SQLException {
        User user = userDAO.findByEmail(email);
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    public User getUserById(int id) throws SQLException {
        return userDAO.findById(id);
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }

    public List<User> getAllMedecins() throws SQLException {
        return userDAO.getAllMedecins();
    }

    public void updateUser(User user) throws SQLException {
        userDAO.update(user);
    }

    public void deleteUser(int id) throws SQLException {
        userDAO.delete(id);
    }

    public List<User> rechercherUtilisateursParNom(String nom) throws SQLException {
        return userDAO.rechercherParNom(nom);
    }

    public List<User> rechercherUtilisateursParEmail(String email) throws SQLException {
        return userDAO.rechercherParEmail(email);
    }

    public List<User> rechercherUtilisateursParRole(String role) throws SQLException {
        return userDAO.rechercherParRole(role);
    }

    // ✅ Méthodes Stream corrigées (appellent les bonnes méthodes)
    public List<User> rechercherUtilisateursParNomStream(String nom) throws SQLException {
        return userDAO.rechercherParNom(nom);
    }

    public List<User> rechercherUtilisateursParEmailStream(String email) throws SQLException {
        return userDAO.rechercherParEmail(email);  // retourne List, pas User
    }

    public List<User> rechercherUtilisateursParRoleStream(String role) throws SQLException {
        return userDAO.findByRole(role);
    }

    public long compterUtilisateursParRole(String role) throws SQLException {
        return userDAO.compterParRole(role);
    }

    public double moyennePoidsPatients() throws SQLException {
        return userDAO.moyennePoidsPatients();
    }
}