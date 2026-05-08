CREATE DATABASE IF NOT EXISTS vitahealth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vitahealth;

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `user` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(180) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'PATIENT',
    roles VARCHAR(255) NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    specialite VARCHAR(120) NULL,
    diplome VARCHAR(180) NULL,
    cin VARCHAR(40) NULL,
    poids DOUBLE NULL,
    taille DOUBLE NULL,
    glycemie DOUBLE NULL,
    tension VARCHAR(50) NULL,
    maladie VARCHAR(180) NULL,
    phone VARCHAR(40) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_role (role),
    INDEX idx_user_email (email)
);

CREATE TABLE IF NOT EXISTS appointment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    date DATETIME NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES `user`(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) REFERENCES `user`(id) ON DELETE CASCADE,
    INDEX idx_appointment_patient (patient_id),
    INDEX idx_appointment_doctor (doctor_id),
    INDEX idx_appointment_date (date)
);

CREATE TABLE IF NOT EXISTS health_profile (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    height DOUBLE NOT NULL,
    weight DOUBLE NOT NULL,
    blood_type VARCHAR(5) NULL,
    allergies TEXT NULL,
    chronic_diseases TEXT NULL,
    emergency_contact VARCHAR(120) NULL,
    emergency_phone VARCHAR(40) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_health_profile_user FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS para_medical (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    poids DOUBLE NOT NULL,
    taille DOUBLE NOT NULL,
    glycemie DOUBLE NOT NULL,
    tension_systolique VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_para_medical_user FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    INDEX idx_para_medical_user (user_id)
);

CREATE TABLE IF NOT EXISTS prescriptions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    medecin_id INT NOT NULL,
    medication_list TEXT NOT NULL,
    instructions TEXT NULL,
    duration VARCHAR(120) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prescriptions_patient FOREIGN KEY (patient_id) REFERENCES `user`(id) ON DELETE CASCADE,
    CONSTRAINT fk_prescriptions_doctor FOREIGN KEY (medecin_id) REFERENCES `user`(id) ON DELETE CASCADE,
    INDEX idx_prescriptions_patient (patient_id),
    INDEX idx_prescriptions_doctor (medecin_id)
);

CREATE TABLE IF NOT EXISTS `event` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(180) NOT NULL,
    description TEXT NULL,
    date DATETIME NOT NULL,
    latitude FLOAT NULL,
    longitude FLOAT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_event_date (date)
);

CREATE TABLE IF NOT EXISTS participation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    event_id INT NOT NULL,
    user_id INT NULL,
    participant_name VARCHAR(160) NOT NULL,
    phone VARCHAR(40) NULL,
    emergency_contact VARCHAR(160) NULL,
    note TEXT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_participation_event FOREIGN KEY (event_id) REFERENCES `event`(id) ON DELETE CASCADE,
    CONSTRAINT fk_participation_user FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE SET NULL,
    UNIQUE KEY uk_participation_event_user (event_id, user_id)
);

CREATE TABLE IF NOT EXISTS forum_categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS forum_posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    author_id INT NOT NULL,
    title VARCHAR(180) NOT NULL,
    content TEXT NOT NULL,
    language VARCHAR(10) DEFAULT 'fr',
    status VARCHAR(30) DEFAULT 'PUBLISHED',
    useful_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_forum_posts_category FOREIGN KEY (category_id) REFERENCES forum_categories(id),
    CONSTRAINT fk_forum_posts_author FOREIGN KEY (author_id) REFERENCES `user`(id)
);

CREATE TABLE IF NOT EXISTS forum_comments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    author_id INT NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(30) DEFAULT 'PUBLISHED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_forum_comments_post FOREIGN KEY (post_id) REFERENCES forum_posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_forum_comments_author FOREIGN KEY (author_id) REFERENCES `user`(id)
);

CREATE TABLE IF NOT EXISTS forum_reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    reporter_id INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status VARCHAR(30) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_forum_reports_post FOREIGN KEY (post_id) REFERENCES forum_posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_forum_reports_user FOREIGN KEY (reporter_id) REFERENCES `user`(id)
);

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `user` (id, email, password, first_name, last_name, role, roles, is_verified, specialite, diplome, cin, poids, taille, glycemie, tension, maladie, phone)
VALUES
    (1, 'admin11@vitahealth.com', 'admin123', 'Admin', 'System', 'ADMIN', 'ADMIN', TRUE, NULL, NULL, 'A000001', NULL, NULL, NULL, NULL, NULL, '20000001'),
    (2, 'doctor@vitahealth.test', 'doctor123', 'Nadia', 'Mansouri', 'DOCTOR', 'DOCTOR', TRUE, 'Cardiologie', 'Doctorat en medecine', 'D000002', NULL, NULL, NULL, NULL, NULL, '20000002'),
    (3, 'patient@vitahealth.test', 'patient123', 'Sami', 'Ben Ali', 'PATIENT', 'PATIENT', TRUE, NULL, NULL, 'P000003', 82.5, 1.76, 1.18, '128/82', 'Diabete type 2', '20000003'),
    (4, 'patient2@vitahealth.test', 'patient123', 'Amina', 'Trabelsi', 'PATIENT', 'PATIENT', TRUE, NULL, NULL, 'P000004', 68.0, 1.64, 0.92, '118/76', 'Hypertension legere', '20000004'),
    (5, 'doctor2@vitahealth.test', 'doctor123', 'Karim', 'Haddad', 'DOCTOR', 'DOCTOR', TRUE, 'Endocrinologie', 'Doctorat en medecine', 'D000005', NULL, NULL, NULL, NULL, NULL, '20000005')
ON DUPLICATE KEY UPDATE email = VALUES(email);

INSERT INTO appointment (id, patient_id, doctor_id, date, reason, status)
VALUES
    (1, 3, 2, DATE_ADD(NOW(), INTERVAL 1 DAY), 'Controle tension et fatigue', 'CONFIRMED'),
    (2, 3, 5, DATE_ADD(NOW(), INTERVAL 3 DAY), 'Suivi glycemie', 'SCHEDULED'),
    (3, 4, 2, DATE_SUB(NOW(), INTERVAL 5 DAY), 'Consultation generale', 'COMPLETED')
ON DUPLICATE KEY UPDATE status = VALUES(status);

INSERT INTO health_profile (id, user_id, height, weight, blood_type, allergies, chronic_diseases, emergency_contact, emergency_phone)
VALUES
    (1, 3, 176, 82.5, 'O+', 'Penicilline', 'Diabete type 2', 'Nour Ben Ali', '21111111'),
    (2, 4, 164, 68.0, 'A+', 'Aucune connue', 'Hypertension legere', 'Youssef Trabelsi', '22222222')
ON DUPLICATE KEY UPDATE weight = VALUES(weight);

INSERT INTO para_medical (id, user_id, poids, taille, glycemie, tension_systolique, created_at)
VALUES
    (1, 3, 84.0, 1.76, 1.32, '132/86', DATE_SUB(NOW(), INTERVAL 10 DAY)),
    (2, 3, 82.5, 1.76, 1.18, '128/82', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (3, 4, 68.0, 1.64, 0.92, '118/76', DATE_SUB(NOW(), INTERVAL 1 DAY))
ON DUPLICATE KEY UPDATE glycemie = VALUES(glycemie);

INSERT INTO prescriptions (id, patient_id, medecin_id, medication_list, instructions, duration, created_at)
VALUES
    (1, 3, 5, 'Metformine 500mg', 'Prendre apres le repas du soir. Surveiller la glycemie.', '30 jours', DATE_SUB(NOW(), INTERVAL 4 DAY)),
    (2, 4, 2, 'Amlodipine 5mg', 'Un comprime chaque matin.', '14 jours', DATE_SUB(NOW(), INTERVAL 8 DAY))
ON DUPLICATE KEY UPDATE duration = VALUES(duration);

INSERT INTO `event` (id, title, description, date, latitude, longitude)
VALUES
    (1, 'Atelier nutrition diabetique', 'Comprendre les index glycemiques et construire une assiette equilibree.', DATE_ADD(NOW(), INTERVAL 7 DAY), 36.8065, 10.1815),
    (2, 'Marche sante cardio', 'Session de sensibilisation et marche encadree.', DATE_ADD(NOW(), INTERVAL 14 DAY), 36.8000, 10.1700),
    (3, 'Gestion du stress', 'Techniques simples de respiration et prevention.', DATE_SUB(NOW(), INTERVAL 12 DAY), 36.8100, 10.1900)
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO participation (id, event_id, user_id, participant_name, phone, emergency_contact, note, created_at)
VALUES
    (1, 1, 3, 'Sami Ben Ali', '20000003', 'Nour Ben Ali', 'Interesse par les menus diabete.', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (2, 2, 4, 'Amina Trabelsi', '20000004', 'Youssef Trabelsi', 'Premiere participation.', DATE_SUB(NOW(), INTERVAL 2 DAY))
ON DUPLICATE KEY UPDATE note = VALUES(note);

INSERT INTO forum_categories (id, name, description)
VALUES
    (1, 'Diabete', 'Echanges autour du diabete et de la glycemie'),
    (2, 'Hypertension', 'Questions et conseils sur la tension arterielle'),
    (3, 'Nutrition', 'Alimentation, regimes et habitudes sante'),
    (4, 'Sante mentale', 'Bien-etre, stress et accompagnement'),
    (5, 'Questions medecins', 'Questions destinees aux medecins verifies'),
    (6, 'Experiences patients', 'Temoignages et parcours de soins'),
    (7, 'Actualites medicales', 'Informations et nouveautes sante')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO forum_posts (id, category_id, author_id, title, content, language, status, useful_count, created_at)
VALUES
    (1, 1, 3, 'Comment stabiliser ma glycemie le matin ?', 'Je remarque une glycemie plus haute au reveil. Quels conseils simples puis-je appliquer avant mon prochain rendez-vous ?', 'fr', 'PUBLISHED', 3, DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (2, 5, 2, 'Rappel important sur les conseils medicaux', 'Les reponses du forum aident a orienter, mais ne remplacent pas une consultation medicale personnalisee.', 'fr', 'PUBLISHED', 5, DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (3, 4, 4, 'Stress et sommeil', 'Depuis quelques semaines mon sommeil est perturbe. Je cherche des routines simples pour me calmer le soir.', 'fr', 'PENDING_REVIEW', 0, DATE_SUB(NOW(), INTERVAL 1 DAY))
ON DUPLICATE KEY UPDATE status = VALUES(status);

INSERT INTO forum_comments (id, post_id, author_id, content, status, created_at)
VALUES
    (1, 1, 5, 'Notez vos mesures pendant quelques jours et evitez les collations sucrees tardives. Parlez-en aussi a votre medecin.', 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (2, 1, 4, 'J ai eu le meme souci, le suivi quotidien m a beaucoup aide.', 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 DAY))
ON DUPLICATE KEY UPDATE content = VALUES(content);

INSERT INTO forum_reports (id, post_id, reporter_id, reason, status)
VALUES
    (1, 3, 1, 'Sujet sensible a verifier par moderation', 'OPEN')
ON DUPLICATE KEY UPDATE status = VALUES(status);
