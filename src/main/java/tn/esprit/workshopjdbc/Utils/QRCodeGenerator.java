package tn.esprit.workshopjdbc.Utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import tn.esprit.workshopjdbc.Entities.User;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QRCodeGenerator {
    
    private static final int WIDTH = 250;
    private static final int HEIGHT = 250;
    
    public static Image generateQRCode(String text) {
        try {
            String safeText = text == null || text.isBlank() ? "VitaHealthFX\nDonnees non disponibles" : text;
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(safeText, BarcodeFormat.QR_CODE, WIDTH, HEIGHT);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Image generateUserQRCode(int userId, String email, String firstName, String lastName, String role) {
        String data = String.format("""
                VITAHEALTHFX USER CARD
                ID: %d
                Name: %s %s
                Role: %s
                Email: %s
                Generated: %s
                """,
                userId, safe(firstName), safe(lastName), safe(role), safe(email),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return generateQRCode(data);
    }

    public static Image generateUserQRCode(User user) {
        return generateQRCode(buildUserPayload(user));
    }

    public static String buildUserPayload(User user) {
        if (user == null) return "VitaHealthFX\nUtilisateur non disponible";

        StringBuilder payload = new StringBuilder();
        payload.append("VITAHEALTHFX USER CARD\n");
        payload.append("ID: ").append(user.getId()).append('\n');
        payload.append("Name: ").append(safe(user.getFullName())).append('\n');
        payload.append("Role: ").append(safe(user.getRole())).append('\n');
        payload.append("Email: ").append(safe(user.getEmail())).append('\n');
        payload.append("Phone: ").append(safe(user.getPhone())).append('\n');
        payload.append("CIN: ").append(safe(user.getCin())).append('\n');
        payload.append("Verified: ").append(user.isVerified() ? "Yes" : "No").append('\n');

        if ("PATIENT".equalsIgnoreCase(user.getRole())) {
            payload.append("Medical Profile:\n");
            payload.append("- Weight: ").append(format(user.getPoids(), " kg")).append('\n');
            payload.append("- Height: ").append(format(user.getTaille(), " m")).append('\n');
            payload.append("- Glycemia: ").append(format(user.getGlycemie(), " g/L")).append('\n');
            payload.append("- Blood pressure: ").append(safe(user.getTension())).append('\n');
            payload.append("- Chronic disease: ").append(safe(user.getMaladie())).append('\n');
        } else if ("DOCTOR".equalsIgnoreCase(user.getRole())) {
            payload.append("Professional Profile:\n");
            payload.append("- Specialty: ").append(safe(user.getSpecialite())).append('\n');
            payload.append("- Diploma: ").append(safe(user.getDiplome())).append('\n');
        }

        payload.append("Generated: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .append('\n');
        payload.append("Source: VitaHealthFX");
        return payload.toString();
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "N/A" : value.trim();
    }

    private static String format(Double value, String unit) {
        return value == null ? "N/A" : value + unit;
    }
}
