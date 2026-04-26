package controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Medecin;
import models.RendezVous;
import models.ReponseRendezVous;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConsultationFicheController {

    @FXML private Label lblTitreFiche, lblDateGeneration;
    @FXML private Label lblChamp1, lblChamp2, lblChamp3, lblChampLong;
    @FXML private Label valId, val1, val2, val3, valLong;

    @FXML
    public void initialize() {
        lblDateGeneration.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    public void setData(Object data) {
        if (data instanceof RendezVous rdv) {
            lblTitreFiche.setText("DÉTAILS DU RENDEZ-VOUS");
            valId.setText("#RDV-" + rdv.getId());
            
            lblChamp1.setText("Patient :");
            val1.setText(rdv.getPatientPrenom() + " " + rdv.getPatientNom());
            
            lblChamp2.setText("Date & Heure :");
            val2.setText(rdv.getDate() + " à " + rdv.getHeure());
            
            lblChamp3.setText("Priorité :");
            val3.setText(rdv.getPriorite().toUpperCase());
            
            lblChampLong.setText("SYMPTÔMES / MOTIF");
            valLong.setText(rdv.getMotif());
            
        } else if (data instanceof Medecin med) {
            lblTitreFiche.setText("FICHE DU PRATICIEN");
            valId.setText("#MED-" + med.getId());
            
            lblChamp1.setText("Nom :");
            val1.setText(med.getPrenom() + " " + med.getNom());
            
            lblChamp2.setText("Spécialité :");
            val2.setText(med.getSpecialite());
            
            lblChamp3.setText("Coordonnées :");
            val3.setText(med.getTelephone() + " / " + med.getEmail());
            
            lblChampLong.setText("BIOGRAPHIE / NOTES");
            valLong.setText("Praticien spécialisé en " + med.getSpecialite() + ". Membre actif du réseau VitalHealth.");
            
        } else if (data instanceof ReponseRendezVous rep) {
            lblTitreFiche.setText("RÉPONSE MÉDICALE");
            valId.setText("#REP-" + rep.getId());
            
            lblChamp1.setText("Référence RDV :");
            val1.setText("#RDV-" + rep.getRendezVousId());
            
            lblChamp2.setText("Type de réponse :");
            val2.setText(rep.getTypeReponse().toUpperCase());
            
            lblChamp3.setText("Date d'émission :");
            val3.setText(rep.getDateReponse().toString());
            
            lblChampLong.setText("MESSAGE DU MÉDECIN");
            valLong.setText(rep.getMessage());
        }
    }

    @FXML
    private void handleTelechargerPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le document PDF");
        fileChooser.setInitialFileName(lblTitreFiche.getText().replace(" ", "_").toLowerCase() + "_" + valId.getText().replace("#", "") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Document PDF", "*.pdf"));
        
        File file = fileChooser.showSaveDialog(valId.getScene().getWindow());
        if (file != null) {
            genererPDF(file);
        }
    }

    private void genererPDF(File file) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Fontes
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.GRAY);
            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.BLACK);
            Font fontSubTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new BaseColor(99, 102, 241));
            Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.DARK_GRAY);
            Font fontValue = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK);
            Font fontText = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);

            // En-tête
            Paragraph header = new Paragraph("VITALHEALTH - SYSTÈME MÉDICAL", fontHeader);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph title = new Paragraph(lblTitreFiche.getText(), fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(10);
            document.add(title);

            document.add(new Chunk(new LineSeparator()));

            // Informations Générales
            Paragraph section1 = new Paragraph("INFORMATIONS GÉNÉRALES", fontSubTitle);
            section1.setSpacingBefore(30);
            section1.setSpacingAfter(15);
            document.add(section1);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 2});

            addTableCell(table, "Référence :", valId.getText(), fontLabel, fontValue);
            addTableCell(table, lblChamp1.getText(), val1.getText(), fontLabel, fontValue);
            addTableCell(table, lblChamp2.getText(), val2.getText(), fontLabel, fontValue);
            addTableCell(table, lblChamp3.getText(), val3.getText(), fontLabel, fontValue);

            document.add(table);

            // Détails longs
            Paragraph section2 = new Paragraph(lblChampLong.getText(), fontSubTitle);
            section2.setSpacingBefore(30);
            section2.setSpacingAfter(10);
            document.add(section2);

            Paragraph content = new Paragraph(valLong.getText(), fontText);
            content.setLeading(20);
            document.add(content);

            // Footer
            document.add(new Chunk(new LineSeparator(0.5f, 100, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -25)));
            
            Paragraph footer = new Paragraph("Document généré le " + lblDateGeneration.getText() + " par VitalHealth Application.", fontHeader);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(40);
            document.add(footer);

            document.close();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Félicitations");
            alert.setHeaderText(null);
            alert.setContentText("Le document PDF a été généré avec succès !");
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Échec de la génération PDF");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void addTableCell(PdfPTable table, String label, String value, Font fl, Font fv) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, fl));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        cellLabel.setPaddingBottom(10);
        table.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Phrase(value, fv));
        cellValue.setBorder(Rectangle.NO_BORDER);
        cellValue.setPaddingBottom(10);
        table.addCell(cellValue);
    }

    @FXML
    private void handleFermer() {
        ((Stage) valId.getScene().getWindow()).close();
    }
}
