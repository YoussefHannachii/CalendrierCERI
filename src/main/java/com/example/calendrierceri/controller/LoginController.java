package com.example.calendrierceri.controller;

import com.example.calendrierceri.HelloApplication;
import com.example.calendrierceri.model.User;
import com.example.calendrierceri.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private UserDAO userDAO;

    public LoginController(Connection connection) {
        this.userDAO = new UserDAO(connection);
    }

    public LoginController() {
        try {
            this.userDAO = new UserDAO(HelloApplication.getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur d'accès à la base de données.");
        }
    }

    @FXML
    protected void onLoginButtonClicked() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            if (userDAO != null) {
                User user = userDAO.getUserByCredentials(username, password);
                if (user != null) {
                    System.out.println("Utilisateur trouvé dans la base de données.");
                    System.out.println("Rôle de l'utilisateur: " + user.getRole());
                    showMainPage(user);
                } else {
                    showError("Identifiants incorrects.");
                }
            } else {
                showError("Erreur d'accès à la base de données.");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace(); // Gérer l'erreur de connexion à la base de données
            showError("Erreur de connexion à la base de données.");
        }
    }

    private void showMainPage(User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/calendrierceri/landingPageView.fxml"));
        Parent landingPage = loader.load();
        LandingPageController landingPageController = loader.getController();

        landingPageController.setCurrentUser(user);

        Stage stage = (Stage) usernameField.getScene().getWindow();


        Scene scene = new Scene(landingPage);

        stage.setScene(scene);
        stage.show();
    }

    private void showError(String errorMessage) {
        // Implémentez ici le code pour afficher le message d'erreur
        System.out.println("Erreur: " + errorMessage);
    }
}
