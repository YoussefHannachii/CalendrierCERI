package com.example.calendrierceri;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class HelloApplication extends Application {
    private double reducedWidth;
    private double reducedHeight;

    @Override
    public void start(Stage stage) throws IOException {
        // Charger la vue de la page de connexion
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = fxmlLoader.load();

        // Obtenir les dimensions de l'écran de la tablette
        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();

        // Réduire les dimensions de l'écran de la tablette
        reducedWidth = screenSize.getWidth() * 0.8;
        reducedHeight = screenSize.getHeight() * 0.8;

        // Créer une nouvelle scène avec la vue chargée et les dimensions ajustées
        Scene scene = new Scene(root, reducedWidth, reducedHeight);

        // Définir la scène sur la fenêtre principale et l'afficher
        stage.setScene(scene);
        stage.setTitle("Connexion");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    // Méthode pour établir une connexion à la base de données MySQL
    public static Connection getConnection() throws SQLException {
        // Remplacez les valeurs par celles de votre base de données
        String url = "jdbc:mysql://localhost:3306/edt";
        String username = "root";
        String password = "Smail@10";

        return DriverManager.getConnection(url, username, password);
    }
}
