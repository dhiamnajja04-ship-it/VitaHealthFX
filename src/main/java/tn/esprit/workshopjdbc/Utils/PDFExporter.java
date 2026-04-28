package tn.esprit.workshopjdbc.Utils;

import tn.esprit.workshopjdbc.Entities.User;
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
        if (users == null || users.isEmpty()) {
            AlertUtils.showWarning("Aucune donnée", "Il n'y a aucun utilisateur à exporter.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.setInitialFileName("utilisateurs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));

        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Titre principal
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("LISTE DES UTILISATEURS - VITA HEALTH", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Sous-titre
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GRAY);
            Paragraph subtitle = new Paragraph("Application de gestion de santé VITA", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);
            document.add(new Paragraph(" "));

            // Ligne de séparation
            Paragraph separator = new Paragraph("___________________________________________________");
            separator.setAlignment(Element.ALIGN_CENTER);
            document.add(separator);
            document.add(new Paragraph(" "));

            // Informations d'export
            Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLUE);
            Paragraph exportInfo = new Paragraph("Exporté le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm:ss")), infoFont);
            exportInfo.setAlignment(Element.ALIGN_RIGHT);
            document.add(exportInfo);
            document.add(new Paragraph(" "));

            // Statistiques
            Font statsFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Paragraph stats = new Paragraph("Nombre total d'utilisateurs : " + users.size(), statsFont);
            document.add(stats);
            document.add(new Paragraph(" "));

            // Tableau des utilisateurs
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(15);
            table.setSpacingAfter(15);
            
            float[] columnWidths = {10, 15, 15, 25, 15, 15, 10};
            table.setWidths(columnWidths);

            String[] headers = {"ID", "Prénom", "Nom", "Email", "Rôle", "Téléphone", "Statut"};
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBackgroundColor(new BaseColor(52, 152, 219));
                cell.setPadding(8);
                table.addCell(cell);
            }

            boolean isEvenRow = false;
            for (User u : users) {
                BaseColor rowColor = isEvenRow ? new BaseColor(240, 240, 240) : BaseColor.WHITE;
                
                // ID
                PdfPCell cellId = new PdfPCell(new Phrase(String.valueOf(u.getId())));
                cellId.setBackgroundColor(rowColor);
                cellId.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellId);
                
                // Prénom
                PdfPCell cellFirstName = new PdfPCell(new Phrase(u.getFirstName() != null ? u.getFirstName() : ""));
                cellFirstName.setBackgroundColor(rowColor);
                table.addCell(cellFirstName);
                
                // Nom
                PdfPCell cellLastName = new PdfPCell(new Phrase(u.getLastName() != null ? u.getLastName() : ""));
                cellLastName.setBackgroundColor(rowColor);
                table.addCell(cellLastName);
                
                // Email
                PdfPCell cellEmail = new PdfPCell(new Phrase(u.getEmail() != null ? u.getEmail() : ""));
                cellEmail.setBackgroundColor(rowColor);
                table.addCell(cellEmail);
                
                // Rôle
                PdfPCell cellRole = new PdfPCell(new Phrase(u.getRole() != null ? u.getRole() : ""));
                cellRole.setBackgroundColor(rowColor);
                cellRole.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellRole);
                
                // Téléphone
                PdfPCell cellPhone = new PdfPCell(new Phrase(u.getPhone() != null ? u.getPhone() : ""));
                cellPhone.setBackgroundColor(rowColor);
                cellPhone.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellPhone);
                
                // Statut - CORRIGÉ : isVerified() au lieu de isActive()
                String status = u.isVerified() ? "Vérifié" : "Non vérifié";
                PdfPCell cellStatus = new PdfPCell(new Phrase(status));
                cellStatus.setBackgroundColor(rowColor);
                cellStatus.setHorizontalAlignment(Element.ALIGN_CENTER);
                if (u.isVerified()) {
                    cellStatus.setBackgroundColor(new BaseColor(144, 238, 144));
                } else {
                    cellStatus.setBackgroundColor(new BaseColor(255, 182, 193));
                }
                table.addCell(cellStatus);
                
                isEvenRow = !isEvenRow;
            }

            document.add(table);
            
            // Pied de page
            document.add(new Paragraph(" "));
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, BaseColor.GRAY);
            Paragraph footer = new Paragraph("Document généré automatiquement par VITA Health System", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            AlertUtils.showInfo("✅ Succès", "PDF exporté avec succès !\n\n📁 Emplacement : " + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("❌ Erreur", "Impossible d'exporter le PDF :\n" + e.getMessage());
        }
    }
}