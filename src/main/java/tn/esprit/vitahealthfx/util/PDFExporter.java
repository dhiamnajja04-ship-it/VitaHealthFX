package tn.esprit.vitahealthfx.util;

import tn.esprit.vitahealthfx.entity.User;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFExporter {

    public static void exportUsersToPDF(List<User> users, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.setInitialFileName("utilisateurs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;

        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Titre
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Liste des utilisateurs - VITA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Date d'export
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph date = new Paragraph("Exporté le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), dateFont);
            document.add(date);
            document.add(new Paragraph(" "));

            // Tableau
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(10);

            // En-têtes
            String[] headers = {"ID", "Prénom", "Nom", "Email", "Rôle", "Téléphone"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Données
            for (User u : users) {
                table.addCell(String.valueOf(u.getId()));
                table.addCell(u.getFirstName() != null ? u.getFirstName() : "");
                table.addCell(u.getLastName() != null ? u.getLastName() : "");
                table.addCell(u.getEmail() != null ? u.getEmail() : "");
                table.addCell(u.getRole() != null ? u.getRole() : "");
                table.addCell(u.getPhone() != null ? u.getPhone() : "");
            }

            document.add(table);
            document.close();

            AlertUtils.showInfo("Succès", "PDF exporté avec succès !\n" + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'exporter le PDF : " + e.getMessage());
        }
    }
}