package org.example.service;

import org.example.entity.User;
import org.example.dao.UserDAO;
import java.sql.SQLException;
import java.util.List;

public class ServiceVitaHealth {
    private UserDAO userDAO = new UserDAO();

    // ========== AUTHENTIFICATION ==========
    public User login(String email, String password) throws SQLException {
        User user = userDAO.findByEmail(email);
        if (user != null && password.equals(user.getPassword())) {
            return user;
        }
        return null;
    }

    // ========== READ ==========
    public User getUserById(int id) throws SQLException {
        return userDAO.findById(id);
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }

    public List<User> getAllMedecins() throws SQLException {
        return userDAO.getAllMedecins();
    }

    // ========== UPDATE ==========
    public void updateUser(User user) throws SQLException {
        userDAO.update(user);
    }

    // ========== DELETE ==========
    public void deleteUser(int id) throws SQLException {
        userDAO.delete(id);
    }

    // ========== RECHERCHE SQL ==========
    public List<User> rechercherUtilisateursParNom(String nom) throws SQLException {
        return userDAO.rechercherParNom(nom);
    }

    public List<User> rechercherUtilisateursParEmail(String email) throws SQLException {
        return userDAO.rechercherParEmail(email);
    }

    public List<User> rechercherUtilisateursParRole(String role) throws SQLException {
        return userDAO.rechercherParRole(role);
    }

    // ========== RECHERCHE STREAM ==========
    public List<User> rechercherUtilisateursParNomStream(String nom) throws SQLException {
        return userDAO.rechercherParNomStream(nom);
    }

    public List<User> rechercherUtilisateursParEmailStream(String email) throws SQLException {
        return userDAO.rechercherParEmailStream(email);
    }

    public List<User> rechercherUtilisateursParRoleStream(String role) throws SQLException {
        return userDAO.rechercherParRoleStream(role);
    }

    // ========== STATISTIQUES ==========
    public long compterUtilisateursParRole(String role) throws SQLException {
        return userDAO.compterParRole(role);
    }

    public double moyennePoidsPatients() throws SQLException {
        return userDAO.moyennePoidsPatients();
    }
}