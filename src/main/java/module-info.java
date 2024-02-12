module com.example.calendrierceri {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.calendrierceri to javafx.fxml;
    exports com.example.calendrierceri;
}