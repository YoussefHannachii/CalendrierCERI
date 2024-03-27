module com.example.calendrierceri {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.calendrierceri to javafx.fxml;
    exports com.example.calendrierceri;
    exports com.example.calendrierceri.controller;
    opens com.example.calendrierceri.controller to javafx.fxml;
    exports com.example.calendrierceri.model;
    opens com.example.calendrierceri.model to javafx.fxml;
    exports com.example.calendrierceri.dao;
    opens com.example.calendrierceri.dao to javafx.fxml;
}