package org.example.entity;

public class User {
    private int id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String role; // "patient", "medecin", "admin"
    private boolean verified; // uniquement ce champ
    private String specialite;
    private String diplome;
    private String cin;
    private Double poids;
    private Double taille;
    private Double glycemie;
    private String tension;
    private String maladie;

    public User() {}

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getDiplome() { return diplome; }
    public void setDiplome(String diplome) { this.diplome = diplome; }

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    public Double getPoids() { return poids; }
    public void setPoids(Double poids) { this.poids = poids; }

    public Double getTaille() { return taille; }
    public void setTaille(Double taille) { this.taille = taille; }

    public Double getGlycemie() { return glycemie; }
    public void setGlycemie(Double glycemie) { this.glycemie = glycemie; }

    public String getTension() { return tension; }
    public void setTension(String tension) { this.tension = tension; }

    public String getMaladie() { return maladie; }
    public void setMaladie(String maladie) { this.maladie = maladie; }
}