package tn.esprit.workshopjdbc.Services;

import tn.esprit.workshopjdbc.Entities.Prescription;
import tn.esprit.workshopjdbc.Entities.User;
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

    private static final String[] ARABIC_FONT_PATHS = {
            "C:/Windows/Fonts/arial.ttf",
            "C:/Windows/Fonts/tahoma.ttf",
            "C:/Windows/Fonts/times.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
            "/System/Library/Fonts/Arial.ttf"
    };

    static {
        Map<String, String> fr = new HashMap<>();
        fr.put("title",        "VitaHealthFX - Prescription Médicale");
        fr.put("patient",      "Patient :");
        fr.put("doctor",       "Médecin :");
        fr.put("date",         "Date :");
        fr.put("medications",  "Médicaments :");
        fr.put("duration",     "Durée :");
        fr.put("instructions", "Instructions :");
        fr.put("footer",       "Document généré automatiquement par VitaHealthFX - valable en pharmacie");
        labels.put("fr", fr);

        Map<String, String> en = new HashMap<>();
        en.put("title",        "VitaHealthFX - Medical Prescription");
        en.put("patient",      "Patient:");
        en.put("doctor",       "Doctor:");
        en.put("date",         "Date:");
        en.put("medications",  "Medications:");
        en.put("duration",     "Duration:");
        en.put("instructions", "Instructions:");
        en.put("footer",       "Document generated automatically by VitaHealthFX - valid in pharmacy");
        labels.put("en", en);

        Map<String, String> ar = new HashMap<>();
        ar.put("title",        "VitaHealthFX - وصفة طبية");
        ar.put("patient",      "المريض :");
        ar.put("doctor",       "الطبيب :");
        ar.put("date",         "التاريخ :");
        ar.put("medications",  "الأدوية :");
        ar.put("duration",     "المدة :");
        ar.put("instructions", "التعليمات :");
        ar.put("footer",       "وثيقة مولدة تلقائياً بواسطة VitaHealthFX - صالحة في الصيدلية");
        labels.put("ar", ar);
    }

    public static void generatePrescriptionPDF(
            Prescription prescription,
            User patient,
            User doctor,
            String outputPath,
            String lang) throws IOException {

        Map<String, String> lbl = labels.getOrDefault(lang, labels.get("fr"));

        // ✅ Traduction via LibreTranslate (sauf si langue source = langue cible)
        String medText  = prescription.getMedicationList();
        String durText  = prescription.getDuration();
        String insText  = prescription.getInstructions();

        if (!"fr".equals(lang)) {
            try {
                TraductionService traducteur = new TraductionService();
                System.out.println("🌐 Traduction en cours vers : " + lang);

                // Traduire les 3 champs en parallèle
                CompletableFuture<String> futureMed = traducteur.traduire(medText, "fr", lang);
                CompletableFuture<String> futureDur = traducteur.traduire(durText, "fr", lang);
                CompletableFuture<String> futureIns = traducteur.traduire(insText, "fr", lang);

                // Attendre tous les résultats
                CompletableFuture.allOf(futureMed, futureDur, futureIns).join();

                String translatedMed = futureMed.get();
                String translatedDur = futureDur.get();
                String translatedIns = futureIns.get();

                // ✅ Utiliser la traduction seulement si elle n'est pas vide
                if (translatedMed != null && !translatedMed.isBlank()) medText = translatedMed;
                if (translatedDur != null && !translatedDur.isBlank()) durText = translatedDur;
                if (translatedIns != null && !translatedIns.isBlank()) insText = translatedIns;

                System.out.println("✅ Traduction terminée.");
            } catch (Exception e) {
                System.err.println("⚠️ Traduction échouée, utilisation du texte original : " + e.getMessage());
                // fallback : on garde les textes originaux
            }
        }

        boolean isArabic = "ar".equals(lang);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            // ✅ Charger la police arabe
            PDType0Font arabicFont = null;
            if (isArabic) {
                for (String path : ARABIC_FONT_PATHS) {
                    File fontFile = new File(path);
                    if (fontFile.exists()) {
                        try {
                            arabicFont = PDType0Font.load(document, fontFile);
                            System.out.println("✅ Police arabe chargée : " + path);
                            break;
                        } catch (IOException e) {
                            System.err.println("Échec police : " + path + " — " + e.getMessage());
                        }
                    }
                }
                if (arabicFont == null) {
                    System.err.println("⚠️ Aucune police arabe trouvée, fallback latin.");
                    isArabic = false;
                }
            }

            final PDType0Font arFont = arabicFont;
            final boolean useAr     = isArabic;

            // Textes finaux (traduits ou originaux)
            final String finalMed = safeText(medText);
            final String finalDur = safeText(durText);
            final String finalIns = safeText(insText);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {

                // ── Titre ──────────────────────────────────────────────
                cs.beginText();
                if (useAr) cs.setFont(arFont, 18);
                else       cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
                cs.newLineAtOffset(50, 750);
                cs.showText(lbl.get("title"));
                cs.endText();

                // Ligne séparatrice
                cs.moveTo(50, 730);
                cs.lineTo(550, 730);
                cs.stroke();

                // ── Patient / Médecin / Date ───────────────────────────
                cs.beginText();
                if (useAr) cs.setFont(arFont, 12);
                else       cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(50, 700);
                cs.showText(lbl.get("patient") + " " + patient.getFullName());
                cs.newLineAtOffset(0, -20);
                cs.showText(lbl.get("doctor") + " Dr. " + doctor.getFullName());
                cs.newLineAtOffset(0, -20);
                cs.showText(lbl.get("date") + " " +
                        prescription.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                cs.endText();

                // ── Label Médicaments ──────────────────────────────────
                cs.beginText();
                if (useAr) cs.setFont(arFont, 12);
                else       cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.newLineAtOffset(50, 640);
                cs.showText(lbl.get("medications"));
                cs.endText();

                // ── Valeur Médicaments ─────────────────────────────────
                cs.beginText();
                if (useAr) cs.setFont(arFont, 12);
                else       cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(50, 620);
                // ✅ Gestion multi-lignes
                for (String line : finalMed.split("\n")) {
                    cs.showText(safeText(line));
                    cs.newLineAtOffset(0, -16);
                }
                cs.endText();

                // ── Label Durée ────────────────────────────────────────
                cs.beginText();
                if (useAr) cs.setFont(arFont, 12);
                else       cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.newLineAtOffset(50, 560);
                cs.showText(lbl.get("duration"));
                cs.endText();

                // ── Valeur Durée ───────────────────────────────────────
                cs.beginText();
                if (useAr) cs.setFont(arFont, 12);
                else       cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(50, 540);
                cs.showText(finalDur);
                cs.endText();

                // ── Label Instructions ─────────────────────────────────
                cs.beginText();
                if (useAr) cs.setFont(arFont, 12);
                else       cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.newLineAtOffset(50, 500);
                cs.showText(lbl.get("instructions"));
                cs.endText();

                // ── Valeur Instructions ────────────────────────────────
                cs.beginText();
                if (useAr) cs.setFont(arFont, 12);
                else       cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(50, 480);
                // ✅ Gestion multi-lignes
                for (String line : finalIns.split("\n")) {
                    cs.showText(safeText(line));
                    cs.newLineAtOffset(0, -16);
                }
                cs.endText();

                // ── Pied de page ───────────────────────────────────────
                cs.beginText();
                if (useAr) cs.setFont(arFont, 10);
                else       cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                cs.newLineAtOffset(50, 50);
                cs.showText(lbl.get("footer"));
                cs.endText();
            }

            document.save(outputPath);
            System.out.println("✅ PDF sauvegardé : " + outputPath);
        }
    }

    private static String safeText(String text) {
        return text != null ? text : "";
    }
}