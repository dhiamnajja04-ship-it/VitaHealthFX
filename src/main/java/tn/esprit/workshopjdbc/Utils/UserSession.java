package tn.esprit.workshopjdbc.Utils;
import tn.esprit.workshopjdbc.Entities.User;
public class UserSession {
    private static User instance;

    public static void setSession(User user) { instance = user; }
    public static User getSession() { return instance; }
    public static void clearSession() { instance = null; }
}