module VitalHJavaFx {
    requires javafx.controls;
    requires javafx.fxml;

    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires mysql.connector.j;

    opens controllers to javafx.fxml;
    opens models to javafx.fxml;
    opens application to javafx.fxml;
    exports controllers;
    exports models;
    exports services;
    exports utils;
    exports application;
}