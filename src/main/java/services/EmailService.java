package services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.activation.DataHandler;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

public class EmailService {

    // IMPORTANT: Remplacez ces valeurs par vos vrais identifiants SMTP (ex: Gmail App Password)
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SENDER_EMAIL = "jebrijihed2@gmail.com";
    private static final String SENDER_PASSWORD = "dadw egym mpza hpmr";

    public static void envoyerEmailConfirmation(String destinataire, String nomPatient, String date, String heure, String medecinNom) {
        if (destinataire == null || destinataire.trim().isEmpty()) {
            System.err.println("Pas d'email défini pour le patient.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject("Confirmation de votre rendez-vous - VitalHealth");

            // Génération du contenu du QR Code
            String qrContent = String.format("Rendez-vous Confirmé\nPatient: %s\nDr: %s\nDate: %s\nHeure: %s",
                                             nomPatient, medecinNom, date, heure);
            byte[] qrCode = generateQRCodeImage(qrContent);

            // Multipart "related" pour l'image inline
            MimeMultipart multipart = new MimeMultipart("related");

            // Partie HTML du message
            BodyPart messageBodyPart = new MimeBodyPart();
            String htmlText = String.format(
                "<h3>Bonjour %s,</h3>" +
                "<p>Nous vous confirmons que votre rendez-vous avec <b>Dr. %s</b> est accepté.</p>" +
                "<p><b>Date :</b> %s<br/><b>Heure :</b> %s</p>" +
                "<p>Veuillez présenter le QR code ci-dessous lors de votre arrivée :</p>" +
                "<img src=\"cid:qrcode\" width=\"250\" height=\"250\" />" +
                "<p>Merci de votre confiance.<br/>L'équipe VitalHealth</p>",
                nomPatient, medecinNom, date, heure
            );
            messageBodyPart.setContent(htmlText, "text/html; charset=utf-8");
            multipart.addBodyPart(messageBodyPart);

            // Partie image (QR Code)
            MimeBodyPart imageBodyPart = new MimeBodyPart();
            ByteArrayDataSource bds = new ByteArrayDataSource(qrCode, "image/png");
            imageBodyPart.setDataHandler(new DataHandler(bds));
            imageBodyPart.setHeader("Content-ID", "<qrcode>");
            imageBodyPart.setDisposition(MimeBodyPart.INLINE);
            multipart.addBodyPart(imageBodyPart);

            message.setContent(multipart);

            // Envoi de l'email
            System.out.println("Tentative d'envoi de l'email vers " + destinataire + "...");
            Transport.send(message);
            System.out.println("Email envoyé avec succès à " + destinataire + " !");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envoie un email de refus au patient avec la cause du refus.
     */
    public static void envoyerEmailRefus(String destinataire, String nomPatient,
                                         String medecinNom, String cause) {
        if (destinataire == null || destinataire.trim().isEmpty()) {
            System.err.println("Pas d'email défini pour le patient (refus).");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject("Votre rendez-vous a été refusé - VitalHealth");

            String htmlText = String.format(
                "<div style=\"font-family:Arial,sans-serif;max-width:600px;margin:auto;border:1px solid #e0e0e0;border-radius:8px;overflow:hidden;\">" +
                "<div style=\"background:#c0392b;padding:20px;text-align:center;\">" +
                "<h2 style=\"color:#fff;margin:0;\">Rendez-vous Refusé</h2></div>" +
                "<div style=\"padding:30px;\">" +
                "<p>Bonjour <b>%s</b>,</p>" +
                "<p>Nous vous informons que votre demande de rendez-vous avec <b>Dr. %s</b> a été <span style=\"color:#c0392b;font-weight:bold;\">refusée</span>.</p>" +
                "<div style=\"background:#fdf2f2;border-left:4px solid #c0392b;padding:15px;border-radius:4px;margin:20px 0;\">" +
                "<b>Motif du refus :</b><br/>%s" +
                "</div>" +
                "<p>Nous vous invitons à reprendre contact avec notre équipe pour planifier un nouveau rendez-vous.</p>" +
                "<p style=\"color:#888;\">Cordialement,<br/>L'équipe VitalHealth</p>" +
                "</div></div>",
                nomPatient, medecinNom, cause
            );

            message.setContent(htmlText, "text/html; charset=utf-8");

            System.out.println("Envoi email refus vers " + destinataire + "...");
            Transport.send(message);
            System.out.println("Email de refus envoyé à " + destinataire + " !");

        } catch (Exception e) {
            System.err.println("Erreur envoi email refus : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envoie un email de report au patient avec le nouveau créneau proposé.
     */
    public static void envoyerEmailReport(String destinataire, String nomPatient,
                                          String medecinNom, String message,
                                          String nouvelleDate, String nouvelleHeure) {
        if (destinataire == null || destinataire.trim().isEmpty()) {
            System.err.println("Pas d'email défini pour le patient (report).");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SENDER_EMAIL));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            msg.setSubject("Votre rendez-vous a été reporté - VitalHealth");

            String nouvelleInfoHtml = (nouvelleDate != null && !nouvelleDate.isEmpty())
                ? "<div style=\"background:#eaf4fb;border-left:4px solid #2980b9;padding:15px;border-radius:4px;margin:20px 0;\">" +
                  "<b>Nouveau créneau proposé :</b><br/>" +
                  "<b>Date :</b> " + nouvelleDate + "<br/>" +
                  "<b>Heure :</b> " + (nouvelleHeure != null ? nouvelleHeure : "-") +
                  "</div>"
                : "";

            String htmlText = String.format(
                "<div style=\"font-family:Arial,sans-serif;max-width:600px;margin:auto;border:1px solid #e0e0e0;border-radius:8px;overflow:hidden;\">" +
                "<div style=\"background:#2980b9;padding:20px;text-align:center;\">" +
                "<h2 style=\"color:#fff;margin:0;\">Rendez-vous Reporté</h2></div>" +
                "<div style=\"padding:30px;\">" +
                "<p>Bonjour <b>%s</b>,</p>" +
                "<p>Nous vous informons que votre rendez-vous avec <b>Dr. %s</b> a été <span style=\"color:#2980b9;font-weight:bold;\">reporté</span>.</p>" +
                "<div style=\"background:#fef9e7;border-left:4px solid #f39c12;padding:15px;border-radius:4px;margin:20px 0;\">" +
                "<b>Message du médecin :</b><br/>%s" +
                "</div>" +
                "%s" +
                "<p>Veuillez confirmer votre disponibilité ou contacter notre équipe.</p>" +
                "<p style=\"color:#888;\">Cordialement,<br/>L'équipe VitalHealth</p>" +
                "</div></div>",
                nomPatient, medecinNom, message, nouvelleInfoHtml
            );

            msg.setContent(htmlText, "text/html; charset=utf-8");

            System.out.println("Envoi email report vers " + destinataire + "...");
            Transport.send(msg);
            System.out.println("Email de report envoyé à " + destinataire + " !");

        } catch (Exception e) {
            System.err.println("Erreur envoi email report : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static byte[] generateQRCodeImage(String text) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 250, 250);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
}
