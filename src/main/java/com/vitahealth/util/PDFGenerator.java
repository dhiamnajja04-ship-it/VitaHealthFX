package com.vitahealth.util;

import com.vitahealth.entity.Prescription;
import com.vitahealth.entity.User;
import com.vitahealth.service.TraductionService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PDFGenerator {

    private static final Map<String, Map<String, String>> labels = new HashMap<>();
    private static final String ARABIC_FONT_PATH = "C:/Windows/Fonts/arial.ttf";

    static {
        // Français
        Map<String, String> fr = new HashMap<>();
        fr.put("title", "VitaHealthFX - Prescription Médicale");
        fr.put("patient", "Patient :");
        fr.put("doctor", "Médecin :");
        fr.put("date", "Date :");
        fr.put("medications", "Médicaments :");
        fr.put("duration", "Durée :");
        fr.put("instructions", "Instructions :");
        fr.put("footer", "Document généré automatiquement par VitaHealthFX - valable en pharmacie");
        labels.put("fr", fr);

        // Anglais
        Map<String, String> en = new HashMap<>();
        en.put("title", "VitaHealthFX - Medical Prescription");
        en.put("patient", "Patient:");
        en.put("doctor", "Doctor:");
        en.put("date", "Date:");
        en.put("medications", "Medications:");
        en.put("duration", "Duration:");
        en.put("instructions", "Instructions:");
        en.put("footer", "Document generated automatically by VitaHealthFX - valid in pharmacy");
        labels.put("en", en);

        // Arabe
        Map<String, String> ar = new HashMap<>();
        ar.put("title", "VitaHealthFX - وصفة طبية");
        ar.put("patient", "المريض :");
        ar.put("doctor", "الطبيب :");
        ar.put("date", "التاريخ :");
        ar.put("medications", "الأدوية :");
        ar.put("duration", "المدة :");
        ar.put("instructions", "التعليمات :");
        ar.put("footer", "وثيقة مولدة تلقائياً بواسطة VitaHealthFX - صالحة في الصيدلية");
        labels.put("ar", ar);
    }

    public static void generatePrescriptionPDF(Prescription prescription, User patient, User doctor, String outputPath, String lang) throws IOException {
        Map<String, String> lbl = labels.getOrDefault(lang, labels.get("en"));
        boolean useArabicFont = "ar".equals(lang);

        // Traduire les champs textuels via LibreTranslate
        TraductionService tradService = new TraductionService();
        // On peut régler l'URL si nécessaire (par défaut localhost:5000)
        // tradService.setApiUrl("http://localhost:5000/translate");

        CompletableFuture<String> futureMed = tradService.traduire(prescription.getMedicationList(), "auto", lang);
        CompletableFuture<String> futureDur = tradService.traduire(prescription.getDuration(), "auto", lang);
        CompletableFuture<String> futureIns = tradService.traduire(prescription.getInstructions(), "auto", lang);

        // Attente des résultats (bloquant)
        String translatedMed = futureMed.join();
        String translatedDur = futureDur.join();
        String translatedIns = futureIns.join();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDType0Font arabicFont = null;
            if (useArabicFont) {
                try {
                    arabicFont = PDType0Font.load(document, new File(ARABIC_FONT_PATH));
                } catch (IOException e) {
                    System.err.println("Police arabe introuvable, utilisation de Helvetica");
                    useArabicFont = false;
                }
            }

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                // Titre
                cs.beginText();
                if (useArabicFont && arabicFont != null) cs.setFont(arabicFont, 18);
                else cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
                cs.newLineAtOffset(50, 750);
                cs.showText(lbl.get("title"));
                cs.endText();

                cs.moveTo(50, 730);
                cs.lineTo(550, 730);
                cs.stroke();

                // Contenu
                cs.beginText();
                if (useArabicFont && arabicFont != null) cs.setFont(arabicFont, 12);
                else cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(50, 700);
                cs.showText(lbl.get("patient") + " " + patient.getFullName());
                cs.newLineAtOffset(0, -20);
                cs.showText(lbl.get("doctor") + " Dr. " + doctor.getFullName());
                cs.newLineAtOffset(0, -20);
                cs.showText(lbl.get("date") + " " + prescription.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                cs.newLineAtOffset(0, -30);
                cs.showText(lbl.get("medications"));
                cs.newLineAtOffset(0, -20);
                cs.showText(translatedMed);
                cs.newLineAtOffset(0, -20);
                cs.showText(lbl.get("duration"));
                cs.newLineAtOffset(0, -20);
                cs.showText(translatedDur);
                cs.newLineAtOffset(0, -20);
                cs.showText(lbl.get("instructions"));
                cs.newLineAtOffset(0, -20);
                cs.showText(translatedIns);
                cs.endText();

                // Pied de page
                cs.beginText();
                if (useArabicFont && arabicFont != null) cs.setFont(arabicFont, 10);
                else cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                cs.newLineAtOffset(50, 50);
                cs.showText(lbl.get("footer"));
                cs.endText();
            }
            document.save(outputPath);
        }
    }
}