package tn.esprit.workshopjdbc.Services;

import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Utils.DbConnection;
import tn.esprit.workshopjdbc.Utils.UserSession;
import java.sql.*;

public class AuthService {
    private Connection cnx = DbConnection.getInstance().getCnx();

    public boolean login(String email, String password) {
        String query = "SELECT * FROM user WHERE email = ? AND password = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, email);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));

                // --- FIXED AREA ---
                // We take the role string from DB and pass it to setRole(String)
                String roleStr = rs.getString("roles");
                if (roleStr != null) {
                    // Clean up brackets if they exist in the DB string
                    roleStr = roleStr.replace("[", "").replace("]", "").replace("\"", "");
                    user.setRole(roleStr);
                }
                // ------------------

                UserSession.setSession(user);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}