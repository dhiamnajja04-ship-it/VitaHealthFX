package com.vitahealth.entity;

import java.time.LocalDateTime;
import com.vitahealth.entity.ParaMedical;

public class ParaMedicalTestManual {
    public static void main(String[] args) {
        System.out.println("=== Tests de ParaMedical ===\n");

        // Test 1 : Constructeur et accesseurs
        ParaMedical pm = new ParaMedical();
        pm.setId(1);
        pm.setPoids(70.5);
        pm.setTaille(1.75);
        pm.setGlycemie(5.6);
        pm.setTensionSystolique("120");

        assertTest(pm.getCreatedAt() != null, "La date de création ne doit pas être null");
        assertTest(pm.getId() == 1, "getId() == 1");
        assertTest(Math.abs(pm.getPoids() - 70.5) < 0.01, "getPoids() == 70.5");
        assertTest(Math.abs(pm.getTaille() - 1.75) < 0.01, "getTaille() == 1.75");
        assertTest(Math.abs(pm.getGlycemie() - 5.6) < 0.01, "getGlycemie() == 5.6");
        assertTest("120".equals(pm.getTensionSystolique()), "getTensionSystolique() == \"120\"");

        // Test 2 : Calcul IMC
        pm.setPoids(70.5);
        pm.setTaille(1.75);
        double imc = pm.getImc();
        assertTest(Math.abs(imc - 23.0) < 0.1, "IMC = 23.0 (valeur réelle : " + imc + ")");

        pm.setPoids(0);
        assertTest(pm.getImc() == null, "IMC null si poids = 0");

        pm.setPoids(70.5);
        pm.setTaille(0);
        assertTest(pm.getImc() == null, "IMC null si taille = 0");

        // Test 3 : Interprétation IMC
        pm.setPoids(50);
        pm.setTaille(1.75);
        assertTest("Insuffisance pondérale".equals(pm.getImcInterpretation()), "Interprétation : Insuffisance pondérale");

        pm.setPoids(65);
        assertTest("Poids normal".equals(pm.getImcInterpretation()), "Interprétation : Poids normal");

        pm.setPoids(80);
        assertTest("Surpoids".equals(pm.getImcInterpretation()), "Interprétation : Surpoids");

        pm.setPoids(95);
        assertTest("Obésité modérée".equals(pm.getImcInterpretation()), "Interprétation : Obésité modérée");

        pm.setPoids(110);
        assertTest("Obésité sévère".equals(pm.getImcInterpretation()), "Interprétation : Obésité sévère");

        pm.setPoids(130);
        assertTest("Obésité morbide".equals(pm.getImcInterpretation()), "Interprétation : Obésité morbide");

        pm.setPoids(0);
        assertTest("Non calculable".equals(pm.getImcInterpretation()), "Interprétation : Non calculable");

        // Test 4 : toString
        pm.setCreatedAt(LocalDateTime.of(2026, 4, 26, 10, 30));
        String str = pm.toString();
        assertTest(str.contains("Paramètres du"), "toString contient 'Paramètres du'");

        System.out.println("\n✅ Tous les tests sont passés !");
    }

    private static void assertTest(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("❌ Échec : " + message);
        } else {
            System.out.println("✓ " + message);
        }
    }
}