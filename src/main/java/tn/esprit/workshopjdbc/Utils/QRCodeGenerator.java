package tn.esprit.workshopjdbc.Utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;

public class QRCodeGenerator {
    
    private static final int WIDTH = 250;
    private static final int HEIGHT = 250;
    
    public static Image generateQRCode(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, WIDTH, HEIGHT);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Image generateUserQRCode(int userId, String email, String firstName, String lastName, String role) {
        String data = String.format("VITAHEALTH://USER?id=%d&email=%s&name=%s+%s&role=%s", 
            userId, email, firstName, lastName, role);
        return generateQRCode(data);
    }
}