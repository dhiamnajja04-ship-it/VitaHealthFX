-- ============================================
-- Base de donnees adaptee au code VitaHealth_Integrated
-- Ce script cree les tables exactement comme le code les attend
-- ============================================

CREATE DATABASE IF NOT EXISTS vitahealth
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE vitahealth;

-- Supprimer les anciennes tables si elles existent (de l'autre schema)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS likes;
DROP TABLE IF EXISTS commentaire;
DROP TABLE IF EXISTS forum;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS reclamation;
DROP TABLE IF EXISTS messenger_messages;
DROP TABLE IF EXISTS medical_record;
DROP TABLE IF EXISTS prescription;
DROP TABLE IF EXISTS para_medical;
DROP TABLE IF EXISTS rendez_vous;
DROP TABLE IF EXISTS participation;
DROP TABLE IF EXISTS evenement;
DROP TABLE IF EXISTS utilisateur;
-- Ne pas supprimer forum_posts/comments/categories car elles seront gerees par le code
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- TABLE USER (comme le code l'attend)
-- ============================================
CREATE TABLE IF NOT EXISTS user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'PATIENT',
    is_verified TINYINT(1) NOT NULL DEFAULT 0,
    specialite VARCHAR(255) DEFAULT NULL,
    diplome VARCHAR(255) DEFAULT NULL,
    cin VARCHAR(255) DEFAULT NULL,
    poids DOUBLE DEFAULT NULL,
    taille DOUBLE DEFAULT NULL,
    glycemie DOUBLE DEFAULT NULL,
    tension VARCHAR(255) DEFAULT NULL,
    maladie VARCHAR(255) DEFAULT NULL,
    phone VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE APPOINTMENT (rendez-vous)
-- ============================================
CREATE TABLE IF NOT EXISTS appointment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    date DATETIME NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE EVENT (evenements)
-- ============================================
CREATE TABLE IF NOT EXISTS `event` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    date DATETIME NOT NULL,
    latitude FLOAT DEFAULT NULL,
    longitude FLOAT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE PARTICIPATION (inscriptions evenements)
-- ============================================
CREATE TABLE IF NOT EXISTS participation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    event_id INT NOT NULL,
    user_id INT DEFAULT NULL,
    participant_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    emergency_contact VARCHAR(255) DEFAULT NULL,
    note TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES `event`(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE PRESCRIPTIONS
-- ============================================
CREATE TABLE IF NOT EXISTS prescriptions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    medication_list TEXT NOT NULL,
    instructions TEXT,
    duration VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TABLE PARA_MEDICAL (parametres medicaux)
-- ============================================
CREATE TABLE IF NOT EXISTS para_medical (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    poids DOUBLE DEFAULT NULL,
    taille DOUBLE DEFAULT NULL,
    glycemie DOUBLE DEFAULT NULL,
    tension VARCHAR(50) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- DONNEES DE TEST
-- ============================================

-- Utilisateurs de test
INSERT INTO user (email, password, first_name, last_name, role, is_verified, specialite, phone) VALUES
('admin@vitahealth.tn', 'admin123', 'Admin', 'Principal', 'ADMIN', 1, NULL, '71234567'),
('medecin1@vitahealth.tn', 'medecin123', 'Ahmed', 'Ben', 'DOCTOR', 1, 'Cardiologie', '71234568'),
('medecin2@vitahealth.tn', 'medecin123', 'Fatma', 'Tounsi', 'DOCTOR', 1, 'Dermatologie', '71234569'),
('patient1@vitahealth.tn', 'patient123', 'Samir', 'Mounir', 'PATIENT', 1, NULL, '71234570'),
('patient2@vitahealth.tn', 'patient123', 'Amel', 'Sassi', 'PATIENT', 1, NULL, '71234571');

-- Evenements de test
INSERT INTO `event` (title, description, date, latitude, longitude) VALUES
('Journee du Diabete', 'Sensibilisation et depistage du diabete', '2026-05-15 09:00:00', 36.8065, 10.1815),
('Marche pour la Sante', 'Marche collective pour promouvoir l activite physique', '2026-06-01 07:00:00', 36.8483, 10.3242);

-- Rendez-vous de test
INSERT INTO appointment (patient_id, doctor_id, date, reason, status) VALUES
(4, 2, '2026-06-10 09:00:00', 'Consultation cardiaque', 'SCHEDULED'),
(5, 3, '2026-06-10 10:00:00', 'Consultation dermatologique', 'CONFIRMED'),
(4, 2, '2026-06-11 14:00:00', 'Suivi cardiaque', 'SCHEDULED');

-- Parametres medicaux de test
INSERT INTO para_medical (user_id, poids, taille, glycemie, tension) VALUES
(4, 80.0, 1.80, 1.01, '12/8');

-- ============================================
-- VERIFICATION
-- ============================================
SELECT 'Base vitahealth prete !' AS status;
SELECT 'Tables creees:' AS info;
SHOW TABLES;
