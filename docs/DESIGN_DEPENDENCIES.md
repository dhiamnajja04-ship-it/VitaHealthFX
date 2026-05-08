# Design Libraries JavaFX Integrees

## AtlantaFX

Dependency ajoutee:

```xml
<dependency>
    <groupId>io.github.mkpaz</groupId>
    <artifactId>atlantafx-base</artifactId>
    <version>2.1.0</version>
</dependency>
```

Utilisation dans le projet:

- `ThemeManager` applique `PrimerLight` en mode clair.
- `ThemeManager` applique `PrimerDark` en mode sombre.
- Les CSS existants `style.css`, `style-light.css`, `style-dark.css` restent au-dessus pour garder l'identite VitaHealth.

Pourquoi ce choix:

- AtlantaFX est adapte aux applications JavaFX desktop.
- Il modernise les controles natifs: boutons, tables, champs, tabs, combo boxes.
- Il ne force pas a refaire toutes les vues FXML.
- Il aide a obtenir un rendu plus professionnel pour la soutenance.

## Libraries recommandees mais pas encore necessaires

| Library | Utilite | Quand l'ajouter |
|---|---|---|
| ControlsFX | Notifications, CheckComboBox, validation, popovers | Si on veut filtres avances et notifications toast natives |
| Ikonli | Icons FontAwesome/Material dans boutons et menus | Si on veut remplacer les emojis par des icones professionnelles |
| TilesFX | Tuiles dashboard, jauges, indicateurs | Si on veut un dashboard medical tres visuel |

