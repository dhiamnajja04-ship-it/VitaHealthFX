package services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
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

            String contenu = String.format(
                "Bonjour %s,\n\n" +
                "Nous vous confirmons que votre rendez-vous avec Dr. %s est accepté.\n" +
                "Date : %s\n" +
                "Heure : %s\n\n" +
                "Merci de votre confiance.\nL'équipe VitalHealth",
                nomPatient, medecinNom, date, heure
            );

            message.setText(contenu);

            // Envoi de l'email en vrai
            System.out.println("Tentative d'envoi de l'email vers " + destinataire + "...");
            Transport.send(message);
            System.out.println("Email envoyé avec succès à " + destinataire + " !");

        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }
}
