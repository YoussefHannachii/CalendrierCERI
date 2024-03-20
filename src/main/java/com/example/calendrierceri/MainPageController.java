package com.example.calendrierceri;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainPageController {
    @FXML
    private Label welcomeLabel;

    private String role;

    public void setRole(String role) {
        this.role = role;
        updateWelcomeMessage();
    }

    private void updateWelcomeMessage() {
        if (role.equals("Professeur")) {
            welcomeLabel.setText("Welcome, Professor!");
        } else if (role.equals("Etudiant")) {
            welcomeLabel.setText("Welcome, Student!");
        }
    }
}
