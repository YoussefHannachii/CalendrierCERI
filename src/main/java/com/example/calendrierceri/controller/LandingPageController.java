package com.example.calendrierceri.controller;

import com.example.calendrierceri.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.scene.control.ToggleButton;
import javafx.stage.WindowEvent;


public class LandingPageController implements Initializable {
    @FXML
    private VBox calendarViewVBox;

    @FXML
    private MenuButton calendarViewType;

    private User currentUser;

    @FXML
    private ToggleButton themeToggle;

    public void setCurrentUser(User user){
        this.currentUser=user;
    }

    private static final String LIGHT_MODE_STYLE = "-fx-background-color: white; -fx-text-fill: black;";
    private static final String DARK_MODE_STYLE = "-fx-background-color: #4C4C4C; -fx-text-fill: white;";


    @Override
    public void initialize(URL url, ResourceBundle rb) {

        themeToggle.setOnAction(event -> {
            if (themeToggle.isSelected()) {
                safelyApplyDarkMode();
            } else {
                safelyApplyLightMode();
            }
        });

            // Appliquer le mode clair par défaut
            calendarViewType.getItems().clear();

            MenuItem weeklyMenuItem = new MenuItem("Weekly");
            MenuItem dailyMenuItem = new MenuItem("Daily");

            // Ajouter les éléments de menu au MenuButton
            calendarViewType.getItems().addAll(weeklyMenuItem, dailyMenuItem);



            weeklyMenuItem.setOnAction(event -> {
                try {
                    FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/com/example/calendrierceri/weeklyCalendarView.fxml"));
                    Node weeklyView = loader2.load();
                    WeeklyCalendarViewController weeklyCalendarViewController = loader2.getController();
                    weeklyCalendarViewController.initializeWeeklyData(currentUser);
                    if (calendarViewVBox.getChildren().isEmpty()) {
                        calendarViewVBox.getChildren().add(weeklyView);
                    } else {
                        // Remplacer le contenu existant
                        calendarViewVBox.getChildren().set(0, weeklyView);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        dailyMenuItem.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/calendrierceri/dailyCalendarView.fxml"));
                Node dailyView = loader.load();
                DailyCalendarViewController dailyCalendarViewController = loader.getController();
                // Utilisez LocalDate.now() ou une autre date si nécessaire
                dailyCalendarViewController.initializeDailyData(currentUser, LocalDate.now());

                if (calendarViewVBox.getChildren().isEmpty()) {
                    calendarViewVBox.getChildren().add(dailyView);
                } else {
                    // Remplacer le contenu existant
                    calendarViewVBox.getChildren().set(0, dailyView);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }
    private void safelyApplyLightMode() {
        Platform.runLater(() -> {
            if (calendarViewVBox.getScene() != null && calendarViewVBox.getScene().getRoot() != null) {
                calendarViewVBox.getScene().getRoot().setStyle(LIGHT_MODE_STYLE);
            }
        });
    }

    private void safelyApplyDarkMode() {
        Platform.runLater(() -> {
            if (calendarViewVBox.getScene() != null && calendarViewVBox.getScene().getRoot() != null) {
                calendarViewVBox.getScene().getRoot().setStyle(DARK_MODE_STYLE);
            }
        });
    }

}
