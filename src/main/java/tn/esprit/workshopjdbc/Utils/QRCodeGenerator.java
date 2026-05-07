package tn.esprit.workshopjdbc.Utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import tn.esprit.workshopjdbc.Entities.User;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;

public class QRCodeGenerator {
    
    private static final int WIDTH = 420;
    private static final int HEIGHT = 420;
    
    public static Image generateQRCode(String text) {
        try {
            String safeText = text == null || text.isBlank() ? "VitaHealthFX\nDonnees non disponibles" : text;
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 2);
            BitMatrix bitMatrix = qrCodeWriter.encode(safeText, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);
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

        StringBuilder note = new StringBuilder();
        note.append("VitaHealthFX ID=").append(user.getId());
        note.append("; Role=").append(safe(user.getRole()));
        note.append("; CIN=").append(safe(user.getCin()));
        note.append("; Verified=").append(user.isVerified() ? "Yes" : "No");

        if ("PATIENT".equalsIgnoreCase(user.getRole())) {
            note.append("; Weight=").append(format(user.getPoids(), " kg"));
            note.append("; Height=").append(format(user.getTaille(), " m"));
            note.append("; Glycemia=").append(format(user.getGlycemie(), " g/L"));
            note.append("; BloodPressure=").append(safe(user.getTension()));
            note.append("; Disease=").append(safe(user.getMaladie()));
        } else if ("DOCTOR".equalsIgnoreCase(user.getRole())) {
            note.append("; Specialty=").append(safe(user.getSpecialite()));
            note.append("; Diploma=").append(safe(user.getDiplome()));
        }

        note.append("; Generated=")
                .append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return """
                BEGIN:VCARD
                VERSION:3.0
                PRODID:-//VitaHealthFX//User QR//FR
                FN:%s
                N:%s;%s;;;
                ORG:VitaHealthFX
                TITLE:%s
                EMAIL:%s
                TEL:%s
                NOTE:%s
                END:VCARD
                """.formatted(
                escapeVCard(safe(user.getFullName())),
                escapeVCard(safe(user.getLastName())),
                escapeVCard(safe(user.getFirstName())),
                escapeVCard(safe(user.getRole())),
                escapeVCard(safe(user.getEmail())),
                escapeVCard(safe(user.getPhone())),
                escapeVCard(note.toString())
        );
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "N/A" : value.trim();
    }

    private static String format(Double value, String unit) {
        return value == null ? "N/A" : value + unit;
    }

    private static String escapeVCard(String value) {
        return safe(value)
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
