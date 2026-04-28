package tn.esprit.workshopjdbc.Entities;

public class User {
    private int id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String role;
    private boolean verified;
    private String specialite;
    private String diplome;
    private String cin;
    private Double poids;
    private Double taille;
    private Double glycemie;
    private String tension;
    private String maladie;
    private String phone;  // ← AJOUT DE TON CHAMP

    public User() {}

    // Getters
    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRole() { return role; }
    public boolean isVerified() { return verified; }
    public String getSpecialite() { return specialite; }
    public String getDiplome() { return diplome; }
    public String getCin() { return cin; }
    public Double getPoids() { return poids; }
    public Double getTaille() { return taille; }
    public Double getGlycemie() { return glycemie; }
    public String getTension() { return tension; }
    public String getMaladie() { return maladie; }
    public String getPhone() { return phone; }  // ← AJOUT DE TON GETTER

    // Setters
    public void setId(int id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setRole(String role) { this.role = role; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
    public void setDiplome(String diplome) { this.diplome = diplome; }
    public void setCin(String cin) { this.cin = cin; }
    public void setPoids(Double poids) { this.poids = poids; }
    public void setTaille(Double taille) { this.taille = taille; }
    public void setGlycemie(Double glycemie) { this.glycemie = glycemie; }
    public void setTension(String tension) { this.tension = tension; }
    public void setMaladie(String maladie) { this.maladie = maladie; }
    public void setPhone(String phone) { this.phone = phone; }  // ← AJOUT DE TON SETTER

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}