package tn.esprit.workshopjdbc.Services;

import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Utils.DbConnection;
import tn.esprit.workshopjdbc.Utils.UserSession;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthService {
    private Connection cnx = DbConnection.getInstance().getCnx();

    public boolean login(String email, String password) {
        // Simple query for testing. Your teammate will likely add BCrypt later.
        String query = "SELECT * FROM user WHERE email = ? AND password = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, email);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setFirstName(rs.getString("first_name")); // Make sure this matches your DB column

                // Handling Roles: This assumes roles are stored as "ROLE_ADMIN" or "ROLE_USER"
                String roleStr = rs.getString("roles");
                List<String> roles = new ArrayList<>();
                roles.add(roleStr.replace("[", "").replace("]", "").replace("\"", ""));
                user.setRoles(roles);

                UserSession.setSession(user);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}