-- Seed data for VitalHealth
USE vital_health;

-- Clear existing data
DELETE FROM reponse_rendez_vous;
DELETE FROM rendez_vous;
DELETE FROM medecin;

-- Reseting auto-increment
ALTER TABLE medecin AUTO_INCREMENT = 1;
ALTER TABLE rendez_vous AUTO_INCREMENT = 1;
ALTER TABLE reponse_rendez_vous AUTO_INCREMENT = 1;

-- INSERT Medecins
INSERT INTO medecin (nom, prenom, specialite, telephone, email) VALUES
('Ben Salem', 'Ahmed', 'Cardiologie', '22113344', 'ahmed.bensalem@gmail.com'),
('Gharbi', 'Myriam', 'Pédiatrie', '55667788', 'm.gharbi@vitalhealth.tn'),
('Trabelsi', 'Sami', 'Dermatologie', '98123456', 'sami.trabelsi@topnet.tn'),
('Karray', 'Ines', 'Gynécologie', '21456789', 'ines.karray@gmail.com');

-- INSERT Rendez-vous
INSERT INTO rendez_vous (date, heure, motif, statut, medecin_id, patient_nom, patient_prenom, patient_tel) VALUES
(CURDATE() + INTERVAL 1 DAY, '09:00:00', 'Consultation annuelle cardiologie', 'en_attente', 1, 'Mansour', 'Ali', '21000111'),
(CURDATE() + INTERVAL 2 DAY, '14:30:00', 'Fièvre persistante enfant', 'en_attente', 2, 'Jridi', 'Salma', '52000222'),
(CURDATE() + INTERVAL 1 DAY, '10:00:00', 'Contrôle dermatologique', 'confirme', 3, 'Belhadj', 'Omar', '98000333'),
(CURDATE() + INTERVAL 3 DAY, '11:15:00', 'Suivi de grossesse', 'en_attente', 4, 'Zouari', 'Faten', '24000444');

-- INSERT Réponses
INSERT INTO reponse_rendez_vous (message, date_reponse, rendez_vous_id, type_reponse) VALUES
('Votre rendez-vous est confirmé pour demain à 10h.', NOW(), 3, 'accepte');
