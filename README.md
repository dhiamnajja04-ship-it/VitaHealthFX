# 📚 Documentation VitaHealthFX

## 🏥 Présentation

**VitaHealthFX** est une application de gestion de santé développée en JavaFX permettant la gestion des patients, médecins, rendez-vous et dossiers médicaux.

---

## 📋 Table des matières

1. [Installation & Configuration](#installation--configuration)
2. [Structure du Projet](#structure-du-projet)
3. [Fonctionnalités](#fonctionnalités)
4. [Configuration Email](#configuration-email)
5. [Comptes de Test](#comptes-de-test)
6. [Utilisation](#utilisation)
7. [Architecture Technique](#architecture-technique)

---

## 🚀 Installation & Configuration

### Prérequis
- **Java 21** (JDK)
- **MySQL/MariaDB** (via XAMPP recommandé)
- **Maven 3.9+**

### Étapes d'installation

1. **Cloner le repository** :
   ```bash
   git clone https://github.com/dhiamnajja04-ship-it/VitaHealthFX.git
   cd VitaHealthFX
   ```

2. **Démarrer MySQL** (via XAMPP Control Panel)

3. **Compiler le projet** :
   ```bash
   mvn clean compile
   ```

4. **Lancer l'application** :
   ```bash
   mvn javafx:run
   ```

---

## 📁 Structure du Projet

```
VitaHealthFX/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── tn/esprit/workshopjdbc/
│   │   │       ├── Controllers/     # Contrôleurs JavaFX
│   │   │       ├── Services/        # Services métier
│   │   │       ├── dao/             # Data Access Objects
│   │   │       ├── Entities/        # Entités JPA
│   │   │       └── Utils/           # Utilitaires
│   │   └── resources/
│   │       ├── fxml/                # Vues FXML
│   │       ├── css/                 # Styles CSS
│   │       └── email-config.properties  # Config email
│   └── test/                        # Tests unitaires
├── pom.xml                          # Configuration Maven
└── DOCUMENTATION.md                 # Ce fichier
```

---

## ✨ Fonctionnalités

### 🔐 Authentification
- ✅ Inscription avec email de confirmation
- ✅ Connexion sécurisée (BCrypt)
- ✅ Réinitialisation de mot de passe par email
- ✅ Vérification CAPTCHA

### 👤 Gestion des Utilisateurs
- **Rôles** : PATIENT, DOCTOR, ADMIN, NUTRITIONIST
- **Profils** : Informations personnelles, photos, documents
- **Vérification** : Email de confirmation à l'inscription

### 📅 Gestion des Rendez-vous
- Prise de rendez-vous avec les médecins
- Gestion du calendrier
- Notifications et rappels

### 📋 Dossiers Médicaux
- Historique des consultations
- Ordonnances médicales
- Suivi des paramètres de santé (poids, taille, glycémie, tension)

### 💬 Forum Communautaire
- Publication de questions
- Commentaires et likes
- Signalement de contenu inapproprié

---

## 📧 Configuration Email

L'application utilise **Gmail SMTP** pour envoyer les emails.

### ⚙️ Configuration Actuelle

**Fichier** : `src/main/resources/email-config.properties`

```properties
email.from=dhiamnajja04@gmail.com
email.password=sjrt tgvd lhak qjnv
```

### 🔧 Comment configurer votre propre email

1. **Activez la vérification en 2 étapes** sur votre compte Google
2. **Créez un mot de passe d'application** :
   - Allez sur https://myaccount.google.com/
   - Sécurité > Mots de passe d'application
   - Sélectionnez "Autre (personnalisé)" → Nommez-le "VitaHealth"
   - Copiez le code généré (16 caractères)
3. **Modifiez le fichier** `email-config.properties`

### 📨 Types d'emails envoyés

| Type | Description |
|------|-------------|
| **Bienvenue** | Email envoyé après inscription |
| **Réinitialisation** | Code de vérification pour mot de passe oublié |
| **Notification** | Rappels de rendez-vous |

---

## 👥 Comptes de Test

### 🔹 Médecin
- **Email** : `medecin@test.com`
- **Mot de passe** : `test123`
- **Nom** : Jean Dupont
- **Spécialité** : Cardiologie

### 🔹 Patient
- **Email** : `patient@test.com`
- **Mot de passe** : `test123`
- **Nom** : Marie Martin
- **Rôle** : Patient

### 🔹 Admin (si configuré)
- Email admin configuré dans la base de données
- Accès à tous les utilisateurs et paramètres

---

## 🎯 Utilisation

### 📝 Inscription

1. Sur l'écran de connexion, cliquez sur **"S'inscrire"**
2. Remplissez le formulaire :
   - Prénom, Nom
   - Email valide
   - Mot de passe (6+ caractères, 1 majuscule, 1 minuscule, 1 chiffre)
   - Téléphone
   - CIN
   - Choisissez le rôle (Patient ou Médecin)
3. **Vous recevrez un email de confirmation**

### 🔐 Connexion

1. Entrez votre email et mot de passe
2. Si vous avez oublié votre mot de passe :
   - Cliquez sur **"Mot de passe oublié ?"**
   - Entrez votre email + CAPTCHA
   - **Vous recevrez un code par email**
   - Saisissez le code et créez un nouveau mot de passe

### 📊 Dashboard

- **Patients** : Voir leurs rendez-vous, dossiers médicaux, paramètres de santé
- **Médecins** : Gérer les rendez-vous, consulter les patients
- **Admin** : Gérer tous les utilisateurs, modérer le forum

---

## 🏗️ Architecture Technique

### 🗄️ Base de Données

**Base** : `vitahealth`

**Tables principales** :
- `user` : Utilisateurs (patients, médecins, admin)
- `appointment` : Rendez-vous
- `medical_record` : Dossiers médicaux
- `prescription` : Ordonnances
- `forum_posts`, `forum_comments` : Forum
- `event`, `participation` : Événements

### 🔧 Technologies Utilisées

| Technologie | Version | Usage |
|------------|---------|-------|
| Java | 21 | Langage principal |
| JavaFX | 21 | Interface graphique |
| Maven | 3.9+ | Gestion des dépendances |
| MySQL | 8.0+ | Base de données |
| BCrypt | 0.4 | Hachage des mots de passe |
| JavaMail | 2.0.3 | Envoi d'emails |
| PDFBox | 2.0.30 | Génération de PDF |
| Jackson | 2.16.1 | JSON parsing |

### 🎨 Architecture MVC

```
┌─────────────┐
│    View     │  ← FXML + CSS
│  (FXML/CSS) │
└──────┬──────┘
       │
┌──────▼──────┐
│  Controller │  ← JavaFX Controllers
│   (Java)    │
└──────┬──────┘
       │
┌──────▼──────┐
│   Service   │  ← Logique métier
│   (Java)    │
└──────┬──────┘
       │
┌──────▼──────┐
│     DAO     │  ← Accès données
│   (Java)    │
└──────┬──────┘
       │
┌──────▼──────┐
│  Database   │  ← MySQL
└─────────────┘
```

---

## 🔒 Sécurité

- **Mots de passe** : Hachage BCrypt avec salt
- **Emails** : Connexion SMTP sécurisée (TLS)
- **CAPTCHA** : Protection contre les robots
- **Vérification email** : Confirmation à l'inscription
- **Rôles** : Contrôle d'accès basé sur les rôles (RBAC)

---

## 🐛 Dépannage

### ❌ Problème : "Communications link failure"
**Solution** : Démarrer MySQL via XAMPP Control Panel

### ❌ Problème : Email non envoyé
**Vérifications** :
1. Configuration `email-config.properties` correcte
2. Mot de passe d'application Gmail valide
3. Vérification en 2 étapes activée sur Gmail

### ❌ Problème : "mvn command not found"
**Solution** : Utiliser Maven Wrapper
```bash
.\apache-maven-3.9.6\bin\mvn.cmd javafx:run
```

---

## 📞 Support

**Email de support** : dhiamnajja04@gmail.com

**Repository GitHub** : https://github.com/dhiamnajja04-ship-it/VitaHealthFX

---

## 📝 Versions

- **Version actuelle** : 1.0-SNAPSHOT
- **Dernière mise à jour** : Mai 2026
- **Branche** : version-avant-final-dhai

---

## 🎉 Remerciements

Développé par l'équipe **VitaHealth** - ESPRIT Workshop JDBC 2024

---

*Documentation mise à jour le 8 Mai 2026*
