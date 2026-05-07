package tn.esprit.workshopjdbc.Services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import tn.esprit.workshopjdbc.Entities.Prescription;
import tn.esprit.workshopjdbc.Entities.User;

import java.util.Properties;

public class EmailService {

    private static final String FROM_EMAIL = "votreemail@gmail.com";
    private static final String PASSWORD = "mot_de_passe_application";

    public static void envoyerNotificationPrescription(
            User patient,
            User docteur,
            Prescription prescription,
            boolean modification
    ) {

        String sujet = modification
                ? "Modification de votre prescription"
                : "Nouvelle prescription médicale";

        String contenu =
                "Bonjour " + patient.getFullName() + ",\n\n" +

                        (modification
                                ? "Votre prescription a été modifiée par Dr. "
                                : "Une nouvelle prescription a été ajoutée par Dr. ")

                        + docteur.getFullName() + ".\n\n" +

                        "Médicaments :\n" +
                        prescription.getMedicationList() + "\n\n" +

                        "Durée : " +
                        prescription.getDuration() + "\n\n" +

                        "Instructions :\n" +
                        prescription.getInstructions() + "\n\n" +

                        "Cordialement,\n" +
                        "VitaHealth";

        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
                    }
                });

        try {

            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(FROM_EMAIL));

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(patient.getEmail())
            );

            message.setSubject(sujet);

            message.setText(contenu);

            Transport.send(message);

            System.out.println("Email envoyé avec succès");

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Erreur envoi email");
        }
    }
}