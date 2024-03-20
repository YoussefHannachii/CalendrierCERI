module com.example.calendrierceri {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.calendrierceri to javafx.fxml;
    exports com.example.calendrierceri;
}