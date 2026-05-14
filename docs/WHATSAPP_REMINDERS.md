# WhatsApp Appointment Reminders

VitaHealthFX integre un service de rappel WhatsApp pour les rendez-vous.

## Fonctionnement

- Dans l'espace Medecin, chaque ligne de rendez-vous contient un bouton `WhatsApp`.
- Si les credentials WhatsApp ne sont pas configures, l'application fonctionne en mode test et affiche le message qui aurait ete envoye.
- Si les credentials sont configures, l'application appelle WhatsApp Cloud API.

## Variables d'environnement

```powershell
$env:WHATSAPP_ACCESS_TOKEN="EAAG..."
$env:WHATSAPP_PHONE_NUMBER_ID="1234567890"
$env:WHATSAPP_GRAPH_VERSION="v20.0"
$env:WHATSAPP_TEMPLATE_NAME="appointment_reminder"
$env:WHATSAPP_LANGUAGE_CODE="fr"
$env:WHATSAPP_MESSAGE_MODE="template"
```

## Template WhatsApp recommande

Nom: `appointment_reminder`

Langue: `fr`

Corps:

```text
Bonjour {{1}}, rappel VitaHealthFX: votre rendez-vous avec Dr. {{2}} est prevu le {{3}}. Motif: {{4}}.
```

Variables:

1. Nom du patient
2. Nom du medecin
3. Date et heure
4. Motif

## Mode texte

Pour tester en conversation ouverte, il est possible d'utiliser:

```powershell
$env:WHATSAPP_MESSAGE_MODE="text"
```

Pour un rappel initie par l'application a un patient qui n'a pas ecrit dans les dernieres 24h, WhatsApp exige generalement un template approuve.

