package tn.esprit.workshopjdbc.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    // Database Config
    private final String URL = "jdbc:mysql://localhost:3306/vitahealth";
    private final String USER = "root";
    private final String PWD = "";

    // Attributes
    private Connection cnx;
    private static DbConnection instance;

    // 1. Private constructor
    private DbConnection() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PWD);
            System.out.println("Connected to VitaHealth DB!");
        } catch (SQLException e) {
            System.err.println("Connection Error: " + e.getMessage());
        }
    }

    // 2. Static method to get the instance
    public static DbConnection getInstance() {
        if (instance == null) {
            instance = new DbConnection();
        }
        return instance;
    }

    // 3. Method to return the SQL connection object
    public Connection getCnx() {
        return cnx;
    }
}