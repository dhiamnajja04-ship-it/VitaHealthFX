module VitalHJavaFx {
    requires javafx.controls;
    requires javafx.fxml;

    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires mysql.connector.j;
    requires jbcrypt;
    requires itextpdf;
    requires jakarta.mail;
    requires com.google.gson;
    requires java.net.http;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires java.desktop;
    requires org.controlsfx.controls;

    opens controllers to javafx.fxml;
    opens models to javafx.fxml, com.google.gson;
    opens application to javafx.fxml;
    opens com.vitahealth.controller to javafx.fxml;
    opens com.vitahealth to javafx.fxml;
    
    exports controllers;
    exports models;
    exports services;
    exports utils;
    exports application;
    exports com.vitahealth;
}