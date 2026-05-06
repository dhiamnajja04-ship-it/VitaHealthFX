# Evaluation VitaHealthFX - Objectif 20/20

Ce document sert de support rapide pour la soutenance du sprint Java. Il mappe le projet VitaHealthFX avec les criteres de la grille fournie.

## Validation individuelle - 20 pts

| Critere | Points vises | Justification dans VitaHealthFX |
|---|---:|---|
| Fonctionnalites avancees | 4/4 | Gestion complete des rendez-vous, profils sante, prescriptions, parametres medicaux, evenements, participations, forum sante, moderation, roles Patient/Medecin/Admin, filtres, tri, pagination, export CSV, QR code et statistiques. |
| Services / API | 4/4 | Services metier et DAO separes: authentification, utilisateurs, rendez-vous, prescriptions, parametres medicaux, evenements, participations, forum, moderation automatique, initialisation schema. Plus de 3 services fonctionnels. |
| Scenario et donnees de test | 3/3 | Base Docker MySQL reproductible avec schema et donnees de test. Comptes Admin, Medecin et Patient disponibles. Scenarios testables: inscription, connexion, prise RDV, gestion RDV, prescription, suivi sante, forum, moderation, export. |
| Maitrise du code et argumentation | 7/7 | Architecture JavaFX MVC/DAO/Service claire, separation UI/metier/donnees, BCrypt pour mot de passe, SessionManager, ThemeManager, Docker pour environnement stable, tests smoke fonctionnels. |
| Interfaces graphiques / UX | 2/2 | Design desktop modernise: login/inscription redesign, dark/light mode, navigation laterale, dashboards cliniques, tables paginees, filtres, cartes statistiques, animations legeres, coherences couleurs VitaHealth. |

**Note individuelle cible: 20/20**

## Validation groupe - projet a 5 personnes

| Critere groupe | Points vises | Preuve / argument |
|---|---:|---|
| GitHub / collaboration | 3/3 | Depot structure, modules integres, branches/commits a presenter par membre. |
| Demonstration, scenario et argumentation | 3/3 | Parcours clair: Admin configure et observe, Medecin gere patients/RDV/prescriptions, Patient prend RDV et suit sa sante, Forum anime la communaute. |
| Qualite UI / charte graphique / UX | 3/3 | Charte medicale navy, vert sante, cyan/violet action; design inspire dashboard clinique; navigation fluide et tables professionnelles. |
| Coherence thematique | 2/2 | Application centree sante: suivi patient, rendez-vous, donnees medicales, prescriptions, forum, evenements de sensibilisation. |
| Fonctionnalites IA | 3/3 | A defendre via moderation automatique du forum, classification/controle de contenu, possibilite d'ajouter recommandation medicale non diagnostique. |
| Integration modules | 6/6 | Modules Patient, Medecin, Admin, Evenements, Participations, Forum, Prescriptions et Parametres medicaux integres dans une seule application JavaFX avec une seule base MySQL. |

**Note groupe cible: 20/20**

## Scenario de demo recommande

1. Lancer Docker MySQL puis VitaHealthFX.
2. Se connecter en Admin et montrer dashboard, utilisateurs, rendez-vous, statistiques, export, moderation forum.
3. Se connecter en Medecin et montrer agenda, confirmation/annulation RDV, patients, prescriptions, parametres medicaux, forum.
4. Se connecter en Patient et montrer prise RDV, filtres, profil sante, parametres, prescriptions, evenements, forum.
5. Montrer inscription Patient/Medecin avec champs metier differents.
6. Basculer dark/light mode et expliquer la charte graphique.
7. Terminer par la base Docker et les donnees de test reproductibles.

## Repartition conseillee pour 5 personnes

| Personne | Module a presenter | Points forts a dire |
|---|---|---|
| Etudiant 1 | Authentification + inscription | Roles, BCrypt, validation metier, dark/light mode. |
| Etudiant 2 | Rendez-vous | Creation, filtres, pagination, statuts, vues Patient/Medecin/Admin. |
| Etudiant 3 | Profil sante + parametres + prescriptions | Donnees medicales, IMC, historique, prescriptions patient. |
| Etudiant 4 | Evenements + participations | Sensibilisation sante, inscription aux ateliers, integration UI. |
| Etudiant 5 | Forum + moderation + admin | Posts, commentaires, signalements, moderation automatique, integration dashboard. |

