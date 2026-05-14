# Guide d'utilisation et configuration APIs - VitaHealthFX

Ce guide explique comment lancer VitaHealthFX et configurer les integrations Google, Facebook et WhatsApp.

## 1. Lancer la base de donnees

Depuis la racine du projet:

```powershell
docker compose up -d
docker compose ps
```

Le service MySQL doit etre `healthy`.

## 2. Lancer l'application

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-25.0.2"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
mvn javafx:run
```

Comptes de test principaux:

- Admin: `admin11@vitahealth.com` / `admin123`
- Medecin: `doctor@vitahealth.test` / `doctor123`
- Patient: `patient@vitahealth.test` / `patient123`

## 3. Configuration Google Login

1. Ouvrir Google Cloud Console.
2. Creer ou selectionner un projet.
3. Activer l'ecran de consentement OAuth.
4. Creer un identifiant OAuth de type application desktop ou application web avec redirect URI:

```text
http://localhost:8085/oauth/callback
```

5. Configurer les variables d'environnement avant de lancer l'application:

```powershell
$env:GOOGLE_CLIENT_ID="votre_google_client_id"
$env:OAUTH_REDIRECT_URI="http://localhost:8085/oauth/callback"
```

Google utilise PKCE dans l'application, donc `GOOGLE_CLIENT_SECRET` n'est pas obligatoire pour le flux desktop.

## 4. Configuration Facebook Login

1. Ouvrir Meta for Developers.
2. Creer une application.
3. Ajouter le produit Facebook Login.
4. Ajouter l'URL de redirection OAuth valide:

```text
http://localhost:8085/oauth/callback
```

5. Configurer les variables:

```powershell
$env:FACEBOOK_APP_ID="votre_facebook_app_id"
$env:FACEBOOK_APP_SECRET="votre_facebook_app_secret"
$env:OAUTH_REDIRECT_URI="http://localhost:8085/oauth/callback"
```

Si l'utilisateur social existe deja en base avec le meme email, il est connecte directement. Sinon, VitaHealthFX ouvre l'inscription avec nom/email pre-remplis, puis demande telephone + OTP.

## 5. Configuration WhatsApp

VitaHealthFX utilise WhatsApp Cloud API pour:

- codes OTP telephone pendant l'inscription
- OTP reset password
- rappels de rendez-vous

Dans Meta for Developers:

1. Creer ou ouvrir une app Meta.
2. Ajouter le produit WhatsApp.
3. Recuperer le `Phone Number ID`.
4. Generer un token d'acces.
5. Configurer:

```powershell
$env:WHATSAPP_GRAPH_VERSION="v20.0"
$env:WHATSAPP_ACCESS_TOKEN="votre_access_token"
$env:WHATSAPP_PHONE_NUMBER_ID="votre_phone_number_id"
```

Format conseille pour les numeros: indicatif pays + numero, par exemple:

```text
+21622111222
```

Sans ces variables, l'application reste fonctionnelle en mode test: elle affiche le code OTP ou le message WhatsApp dans une notification locale.

## 6. Verification rapide

```powershell
mvn -q -DskipTests compile
```

Tests smoke disponibles apres compilation:

```powershell
$cp = Get-Content target\classpath.txt
java -cp "target\classes;target;$cp" FxmlSmoke
java -cp "target\classes;target;$cp" FunctionalSmoke
java -cp "target\classes;target;$cp" OtpSmoke
java -cp "target\classes;target;$cp" WhatsAppReminderSmoke
java -cp "target\classes;target;$cp" SocialAuthSmoke
```

## 7. Notes importantes

- Ne jamais committer les tokens ou secrets dans Git.
- Les variables d'environnement doivent etre definies dans le meme terminal qui lance `mvn javafx:run`.
- Pour Facebook en mode developpement, seuls les comptes ajoutes comme testeurs/developpeurs peuvent se connecter.
- Si le port `8085` est occupe, changer `OAUTH_REDIRECT_URI` et mettre la meme URL dans Google/Meta.
