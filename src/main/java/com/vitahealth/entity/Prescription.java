package com.vitahealth.entity;

import java.time.LocalDateTime;

public class Prescription {
    private int id;
    private LocalDateTime createdAt;
    private String medicationList;
    private String instructions;
    private String duration;

    public Prescription() {
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getMedicationList() { return medicationList; }
    public void setMedicationList(String medicationList) { this.medicationList = medicationList; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    @Override
    public String toString() {
        return "Prescription du " + createdAt + " - " + duration;
    }
}