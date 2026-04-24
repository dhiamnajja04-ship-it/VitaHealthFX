package models;

import java.time.LocalDate;
import java.time.LocalTime;

public class RendezVous {

    private int       id;
    private LocalDate date;
    private LocalTime heure;
    private String    motif;
    private String    statut;      // "en_attente" | "confirme" | "annule"
    private String    priorite;    // "Normale" | "Urgente" | "Basse"
    private int       medecinId;
    private String    patientNom;
    private String    patientPrenom;
    private String    patientTel;

    public RendezVous() {
        this.statut = "en_attente";
        this.priorite = "Normale";
    }

    public RendezVous(int id, LocalDate date, LocalTime heure,
                      String motif, String statut, String priorite, int medecinId,
                      String patientNom, String patientPrenom, String patientTel) {
        this.id           = id;
        this.date         = date;
        this.heure        = heure;
        this.motif        = motif;
        this.statut       = statut;
        this.priorite     = priorite;
        this.medecinId    = medecinId;
        this.patientNom   = patientNom;
        this.patientPrenom = patientPrenom;
        this.patientTel   = patientTel;
    }

    public int       getId()                { return id; }
    public void      setId(int id)          { this.id = id; }

    public LocalDate getDate()              { return date; }
    public void      setDate(LocalDate d)   { this.date = d; }

    public LocalTime getHeure()             { return heure; }
    public void      setHeure(LocalTime h)  { this.heure = h; }

    public String getMotif()                { return motif; }
    public void   setMotif(String motif)    { this.motif = motif; }

    public String getStatut()               { return statut; }
    public void   setStatut(String statut)  { this.statut = statut; }

    public String getPriorite()                { return priorite; }
    public void   setPriorite(String priorite) { this.priorite = priorite; }

    public int  getMedecinId()                  { return medecinId; }
    public void setMedecinId(int medecinId)     { this.medecinId = medecinId; }

    public String getPatientNom()                   { return patientNom; }
    public void   setPatientNom(String patientNom)  { this.patientNom = patientNom; }

    public String getPatientPrenom()                        { return patientPrenom; }
    public void   setPatientPrenom(String patientPrenom)    { this.patientPrenom = patientPrenom; }

    public String getPatientTel()                   { return patientTel; }
    public void   setPatientTel(String patientTel)  { this.patientTel = patientTel; }

    @Override
    public String toString() {
        return "RDV #" + id + " [" + priorite + "] - " + patientPrenom + " " + patientNom
                + " le " + date + " à " + heure;
    }
}