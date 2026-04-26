package com.vitahealth.entity;

import java.time.LocalDateTime;

public class ParaMedical {
    private int id;
    private double poids;
    private double taille;
    private double glycemie;
    private String tensionSystolique;
    private LocalDateTime createdAt;

    public ParaMedical() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters et setters (générés automatiquement)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getPoids() { return poids; }
    public void setPoids(double poids) { this.poids = poids; }
    public double getTaille() { return taille; }
    public void setTaille(double taille) { this.taille = taille; }
    public double getGlycemie() { return glycemie; }
    public void setGlycemie(double glycemie) { this.glycemie = glycemie; }
    public String getTensionSystolique() { return tensionSystolique; }
    public void setTensionSystolique(String tensionSystolique) { this.tensionSystolique = tensionSystolique; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Double getImc() {
        if (poids > 0 && taille > 0) {
            return Math.round((poids / (taille * taille)) * 10.0) / 10.0;
        }
        return null;
    }

    public String getImcInterpretation() {
        Double imc = getImc();
        if (imc == null) return "Non calculable";
        if (imc < 18.5) return "Insuffisance pondérale";
        if (imc < 25) return "Poids normal";
        if (imc < 30) return "Surpoids";
        if (imc < 35) return "Obésité modérée";
        if (imc < 40) return "Obésité sévère";
        return "Obésité morbide";
    }

    @Override
    public String toString() {
        return "Paramètres du " + createdAt;
    }
}