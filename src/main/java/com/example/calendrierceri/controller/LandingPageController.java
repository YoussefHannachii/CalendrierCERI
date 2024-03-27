package com.example.calendrierceri.controller;

import com.example.calendrierceri.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LandingPageController implements Initializable {
    @FXML
    private VBox calendarViewVBox;

    @FXML
    private MenuButton calendarViewType;

    private User currentUser;

    public void setCurrentUser(User user){
        this.currentUser=user;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

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
                    Node dailyView = FXMLLoader.load(getClass().getResource("/com/example/calendrierceri/dailyCalendarView.fxml"));
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
}
