package com.vitahealth.entity;

/**
 * Centralised role constants — use these everywhere instead of raw strings.
 * This eliminates the "PATIENT" vs "patient" vs "DOCTOR" vs "medecin" mismatch.
 */
public final class Role {
    public static final String ADMIN   = "ADMIN";
    public static final String PATIENT = "PATIENT";
    public static final String MEDECIN = "MEDECIN";   // was "medecin" / "DOCTOR" — unified here

    private Role() {}
}