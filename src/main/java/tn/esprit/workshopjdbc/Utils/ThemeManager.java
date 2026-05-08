package tn.esprit.workshopjdbc.Utils;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.scene.Scene;

public final class ThemeManager {
    private static boolean darkMode = false;

    private ThemeManager() {}

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void toggle(Scene scene) {
        darkMode = !darkMode;
        apply(scene);
    }

    public static void apply(Scene scene) {
        if (scene == null) return;
        applyAtlantaTheme();
        scene.getStylesheets().clear();
        add(scene, "/css/style.css");
        add(scene, darkMode ? "/css/style-dark.css" : "/css/style-light.css");
    }

    public static void applyModern(Scene scene) {
        if (scene == null) return;
        applyAtlantaTheme();
        scene.getStylesheets().clear();
        add(scene, "/css/style-modern.css");
        add(scene, darkMode ? "/css/style-dark.css" : "/css/style-light.css");
    }

    public static String toggleText() {
        return darkMode ? "Light mode" : "Dark mode";
    }

    private static void add(Scene scene, String path) {
        String css = ThemeManager.class.getResource(path).toExternalForm();
        scene.getStylesheets().add(css);
    }

    private static void applyAtlantaTheme() {
        Application.setUserAgentStylesheet(darkMode
                ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet());
    }
}
