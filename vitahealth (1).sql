-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : jeu. 07 mai 2026 à 20:56
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `vitahealth`
--

-- --------------------------------------------------------

--
-- Structure de la table `appointment`
--

CREATE TABLE `appointment` (
  `id` int(11) NOT NULL,
  `patient_id` int(11) NOT NULL,
  `doctor_id` int(11) NOT NULL,
  `date` datetime NOT NULL,
  `reason` varchar(255) NOT NULL,
  `status` varchar(50) DEFAULT 'SCHEDULED',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `appointment`
--

INSERT INTO `appointment` (`id`, `patient_id`, `doctor_id`, `date`, `reason`, `status`, `created_at`) VALUES
(1, 4, 2, '2026-06-10 09:00:00', 'Consultation cardiaque', 'SCHEDULED', '2026-05-06 16:08:38'),
(2, 5, 3, '2026-06-10 10:00:00', 'Consultation dermatologique', 'CONFIRMED', '2026-05-06 16:08:38'),
(3, 4, 2, '2026-06-11 14:00:00', 'Suivi cardiaque', 'SCHEDULED', '2026-05-06 16:08:38');

-- --------------------------------------------------------

--
-- Structure de la table `event`
--

CREATE TABLE `event` (
  `id` int(11) NOT NULL,
  `title` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `date` datetime NOT NULL,
  `latitude` float DEFAULT NULL,
  `longitude` float DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `event`
--

INSERT INTO `event` (`id`, `title`, `description`, `date`, `latitude`, `longitude`, `created_at`) VALUES
(1, 'Journee du Diabete', 'Sensibilisation et depistage du diabete', '2026-05-15 09:00:00', 36.8065, 10.1815, '2026-05-06 16:08:38'),
(2, 'Marche pour la Sante', 'Marche collective pour promouvoir l activite physique', '2026-06-01 07:00:00', 36.8483, 10.3242, '2026-05-06 16:08:38');

-- --------------------------------------------------------

--
-- Structure de la table `forum_categories`
--

CREATE TABLE `forum_categories` (
  `id` int(11) NOT NULL,
  `name` varchar(80) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `forum_categories`
--

INSERT INTO `forum_categories` (`id`, `name`, `description`, `created_at`) VALUES
(1, 'Diabete', 'Echanges autour du diabete et de la glycemie', '2026-05-06 16:10:34'),
(2, 'Hypertension', 'Questions et conseils sur la tension arterielle', '2026-05-06 16:10:34'),
(3, 'Nutrition', 'Alimentation, regimes et habitudes sante', '2026-05-06 16:10:34'),
(4, 'Sante mentale', 'Bien-etre, stress et accompagnement', '2026-05-06 16:10:34'),
(5, 'Questions medecins', 'Questions destinees aux medecins verifies', '2026-05-06 16:10:34'),
(6, 'Experiences patients', 'Temoignages et parcours de soins', '2026-05-06 16:10:34'),
(7, 'Actualites medicales', 'Informations et nouveautes sante', '2026-05-06 16:10:34');

-- --------------------------------------------------------

--
-- Structure de la table `forum_comments`
--

CREATE TABLE `forum_comments` (
  `id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL,
  `author_id` int(11) NOT NULL,
  `content` text NOT NULL,
  `status` varchar(30) DEFAULT 'PUBLISHED',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `parent_comment_id` int(11) DEFAULT NULL,
  `like_count` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `forum_comments`
--

INSERT INTO `forum_comments` (`id`, `post_id`, `author_id`, `content`, `status`, `created_at`, `parent_comment_id`, `like_count`) VALUES
(1, 2, 3, 'vznpipff', 'PUBLISHED', '2026-05-07 08:44:08', NULL, 0);

-- --------------------------------------------------------

--
-- Structure de la table `forum_comment_likes`
--

CREATE TABLE `forum_comment_likes` (
  `id` int(11) NOT NULL,
  `comment_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `forum_likes`
--

CREATE TABLE `forum_likes` (
  `id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `forum_likes`
--

INSERT INTO `forum_likes` (`id`, `post_id`, `user_id`, `created_at`) VALUES
(2, 1, 2, '2026-05-06 21:05:02');

-- --------------------------------------------------------

--
-- Structure de la table `forum_posts`
--

CREATE TABLE `forum_posts` (
  `id` int(11) NOT NULL,
  `category_id` int(11) NOT NULL,
  `author_id` int(11) NOT NULL,
  `title` varchar(180) NOT NULL,
  `content` text NOT NULL,
  `language` varchar(10) DEFAULT 'fr',
  `status` varchar(30) DEFAULT 'PUBLISHED',
  `useful_count` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `image_url` varchar(500) DEFAULT NULL,
  `video_url` varchar(500) DEFAULT NULL,
  `tag` varchar(50) DEFAULT NULL,
  `like_count` int(11) DEFAULT 0,
  `share_count` int(11) DEFAULT 0,
  `comment_count` int(11) DEFAULT 0,
  `report_count` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `forum_posts`
--

INSERT INTO `forum_posts` (`id`, `category_id`, `author_id`, `title`, `content`, `language`, `status`, `useful_count`, `created_at`, `updated_at`, `image_url`, `video_url`, `tag`, `like_count`, `share_count`, `comment_count`, `report_count`) VALUES
(1, 2, 2, 'dbabfjzqbf', 'flzfoqbfq', 'fr', 'PUBLISHED', 0, '2026-05-06 21:04:35', '2026-05-06 21:05:02', NULL, NULL, NULL, 1, 0, 0, 0),
(2, 3, 3, 'gzngozbgo', 'c zfngkngm', 'fr', 'PUBLISHED', 0, '2026-05-07 08:43:50', '2026-05-07 17:24:59', NULL, NULL, NULL, 0, 4, 0, 0);

-- --------------------------------------------------------

--
-- Structure de la table `forum_reports`
--

CREATE TABLE `forum_reports` (
  `id` int(11) NOT NULL,
  `post_id` int(11) NOT NULL,
  `reporter_id` int(11) NOT NULL,
  `reason` varchar(255) NOT NULL,
  `status` varchar(30) DEFAULT 'OPEN',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `para_medical`
--

CREATE TABLE `para_medical` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `poids` double DEFAULT NULL,
  `taille` double DEFAULT NULL,
  `glycemie` double DEFAULT NULL,
  `tension` varchar(50) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `para_medical`
--

INSERT INTO `para_medical` (`id`, `user_id`, `poids`, `taille`, `glycemie`, `tension`, `created_at`) VALUES
(1, 4, 80, 1.8, 1.01, '12/8', '2026-05-06 16:08:38');

-- --------------------------------------------------------

--
-- Structure de la table `participation`
--

CREATE TABLE `participation` (
  `id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `participant_name` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `emergency_contact` varchar(255) DEFAULT NULL,
  `note` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `prescriptions`
--

CREATE TABLE `prescriptions` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `medication_list` text NOT NULL,
  `instructions` text DEFAULT NULL,
  `duration` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `role` varchar(50) NOT NULL DEFAULT 'PATIENT',
  `is_verified` tinyint(1) NOT NULL DEFAULT 0,
  `specialite` varchar(255) DEFAULT NULL,
  `diplome` varchar(255) DEFAULT NULL,
  `cin` varchar(255) DEFAULT NULL,
  `poids` double DEFAULT NULL,
  `taille` double DEFAULT NULL,
  `glycemie` double DEFAULT NULL,
  `tension` varchar(255) DEFAULT NULL,
  `maladie` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `user`
--

INSERT INTO `user` (`id`, `email`, `password`, `first_name`, `last_name`, `role`, `is_verified`, `specialite`, `diplome`, `cin`, `poids`, `taille`, `glycemie`, `tension`, `maladie`, `phone`, `created_at`, `updated_at`) VALUES
(1, 'admin@vitahealth.tn', '$2a$10$kSTdqG9sr1sv.reWXIHmHeZvPi/UpmSnTdAs0z8PxF9lR1hqiACs.', 'Admin', 'Principal', 'ADMIN', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '71234567', '2026-05-06 16:08:38', '2026-05-06 16:10:33'),
(2, 'medecin1@vitahealth.tn', '$2a$10$6/8VdSDBwOijUERoOF6IKegZzA8RvPj7iwRSFhzP3NSbm2m0thtbO', 'Ahmed', 'Ben', 'DOCTOR', 1, 'Cardiologie', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '71234568', '2026-05-06 16:08:38', '2026-05-06 20:58:04'),
(3, 'medecin2@vitahealth.tn', '$2a$10$h2SX4v3HQ5KXRt.EuIqkeucMUbZ4yOA.rq2vxM72L0FAA8W1MgyuK', 'Fatma', 'Tounsi', 'DOCTOR', 1, 'Dermatologie', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '71234569', '2026-05-06 16:08:38', '2026-05-07 08:42:38'),
(4, 'patient1@vitahealth.tn', 'patient123', 'Samir', 'Mounir', 'PATIENT', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '71234570', '2026-05-06 16:08:38', '2026-05-06 16:08:38'),
(5, 'patient2@vitahealth.tn', '$2a$10$oRDAuOx.XWqdh3qWcs6lReeYqE7B.wx7y46y.HDoA.71zW3uAEgKa', 'Amel', 'Sassi', 'PATIENT', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '71234571', '2026-05-06 16:08:38', '2026-05-07 09:35:19'),
(6, 'lazaarhamza@gmail.com', '$2a$10$3cdX.yO0yQOizfVsVOMikuSp9mTuWgK6lqpA9rePyV.Tw68MProla', 'hamza', 'lazaar', 'DOCTOR', 0, 'vivug', 'ugutcfc', '14669315', NULL, NULL, NULL, NULL, NULL, '23995376', '2026-05-07 13:03:19', '2026-05-07 13:03:19');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `appointment`
--
ALTER TABLE `appointment`
  ADD PRIMARY KEY (`id`),
  ADD KEY `patient_id` (`patient_id`),
  ADD KEY `doctor_id` (`doctor_id`);

--
-- Index pour la table `event`
--
ALTER TABLE `event`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `forum_categories`
--
ALTER TABLE `forum_categories`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Index pour la table `forum_comments`
--
ALTER TABLE `forum_comments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_forum_comments_post` (`post_id`),
  ADD KEY `fk_forum_comments_author` (`author_id`);

--
-- Index pour la table `forum_comment_likes`
--
ALTER TABLE `forum_comment_likes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_comment_user` (`comment_id`,`user_id`),
  ADD KEY `fk_forum_comment_likes_user` (`user_id`);

--
-- Index pour la table `forum_likes`
--
ALTER TABLE `forum_likes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_post_user` (`post_id`,`user_id`),
  ADD KEY `fk_forum_likes_user` (`user_id`);

--
-- Index pour la table `forum_posts`
--
ALTER TABLE `forum_posts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_forum_posts_category` (`category_id`),
  ADD KEY `fk_forum_posts_author` (`author_id`);

--
-- Index pour la table `forum_reports`
--
ALTER TABLE `forum_reports`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_forum_reports_post` (`post_id`),
  ADD KEY `fk_forum_reports_user` (`reporter_id`);

--
-- Index pour la table `para_medical`
--
ALTER TABLE `para_medical`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Index pour la table `participation`
--
ALTER TABLE `participation`
  ADD PRIMARY KEY (`id`),
  ADD KEY `event_id` (`event_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Index pour la table `prescriptions`
--
ALTER TABLE `prescriptions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Index pour la table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `appointment`
--
ALTER TABLE `appointment`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `event`
--
ALTER TABLE `event`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `forum_categories`
--
ALTER TABLE `forum_categories`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=94;

--
-- AUTO_INCREMENT pour la table `forum_comments`
--
ALTER TABLE `forum_comments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT pour la table `forum_comment_likes`
--
ALTER TABLE `forum_comment_likes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `forum_likes`
--
ALTER TABLE `forum_likes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT pour la table `forum_posts`
--
ALTER TABLE `forum_posts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `forum_reports`
--
ALTER TABLE `forum_reports`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `para_medical`
--
ALTER TABLE `para_medical`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT pour la table `participation`
--
ALTER TABLE `participation`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `prescriptions`
--
ALTER TABLE `prescriptions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `appointment`
--
ALTER TABLE `appointment`
  ADD CONSTRAINT `appointment_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `appointment_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `forum_comments`
--
ALTER TABLE `forum_comments`
  ADD CONSTRAINT `fk_forum_comments_author` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `fk_forum_comments_post` FOREIGN KEY (`post_id`) REFERENCES `forum_posts` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `forum_comment_likes`
--
ALTER TABLE `forum_comment_likes`
  ADD CONSTRAINT `fk_forum_comment_likes_comment` FOREIGN KEY (`comment_id`) REFERENCES `forum_comments` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_forum_comment_likes_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `forum_likes`
--
ALTER TABLE `forum_likes`
  ADD CONSTRAINT `fk_forum_likes_post` FOREIGN KEY (`post_id`) REFERENCES `forum_posts` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_forum_likes_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `forum_posts`
--
ALTER TABLE `forum_posts`
  ADD CONSTRAINT `fk_forum_posts_author` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `fk_forum_posts_category` FOREIGN KEY (`category_id`) REFERENCES `forum_categories` (`id`);

--
-- Contraintes pour la table `forum_reports`
--
ALTER TABLE `forum_reports`
  ADD CONSTRAINT `fk_forum_reports_post` FOREIGN KEY (`post_id`) REFERENCES `forum_posts` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_forum_reports_user` FOREIGN KEY (`reporter_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `para_medical`
--
ALTER TABLE `para_medical`
  ADD CONSTRAINT `para_medical_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `participation`
--
ALTER TABLE `participation`
  ADD CONSTRAINT `participation_ibfk_1` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `participation_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL;

--
-- Contraintes pour la table `prescriptions`
--
ALTER TABLE `prescriptions`
  ADD CONSTRAINT `prescriptions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
