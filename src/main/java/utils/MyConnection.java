package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/vital_health";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static MyConnection instance;
    private Connection connection;

    private MyConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion MySQL établie avec succès.");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Erreur : Driver MySQL introuvable. Assurez-vous d'avoir mysql-connector-j dans le pom.xml.");
            throw new SQLException(e);
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion MySQL : " + e.getMessage());
            System.err.println("   --> Vérifiez que XAMPP est lancé et que la base 'vital_health' existe.");
            throw e;
        }
    }

    public static MyConnection getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            instance = new MyConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}