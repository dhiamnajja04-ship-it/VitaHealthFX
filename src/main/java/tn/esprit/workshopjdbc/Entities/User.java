package tn.esprit.workshopjdbc.Entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {
    // Basic Info
    private Integer id;
    private String email;
    private List<String> roles;
    private String password;
    private String firstName;
    private String lastName;

    // Doctor Fields
    private String cinPhoto;
    private String diplome;
    private String specialite;
    private boolean isVerified;

    // Patient Fields
    private String maladie;
    private Float poids;
    private Float taille;
    private Float glycemie;
    private String tension;
    private LocalDateTime lastParameterAt;

    // File/Meta Management
    private String imageName;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    // Relations


    public User() {


        this.isVerified = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastParameterAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getRoles() {
        if (!roles.contains("ROLE_USER")) {
            roles.add("ROLE_USER");
        }
        return roles;
    }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return firstName + " " + lastName; }

    // Doctor Getters/Setters
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getCinPhoto() { return cinPhoto; }
    public void setCinPhoto(String cinPhoto) { this.cinPhoto = cinPhoto; }

    public String getDiplome() { return diplome; }
    public void setDiplome(String diplome) { this.diplome = diplome; }

    // Patient Getters/Setters
    public String getMaladie() { return maladie; }
    public void setMaladie(String maladie) { this.maladie = maladie; }

    public Float getPoids() { return poids; }
    public void setPoids(Float poids) { this.poids = poids; }

    public Float getTaille() { return taille; }
    public void setTaille(Float taille) { this.taille = taille; }

    public Float getGlycemie() { return glycemie; }
    public void setGlycemie(Float glycemie) { this.glycemie = glycemie; }

    public String getTension() { return tension; }
    public void setTension(String tension) { this.tension = tension; }

    public LocalDateTime getLastParameterAt() { return lastParameterAt; }
    public void setLastParameterAt(LocalDateTime lastParameterAt) { this.lastParameterAt = lastParameterAt; }

    // Meta/Files
    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Relationships (Simplified)


    @Override
    public String toString() {
        return getFullName();
    }
}