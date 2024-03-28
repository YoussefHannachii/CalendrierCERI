package com.example.calendrierceri.controller;

import com.example.calendrierceri.model.User;
import com.example.calendrierceri.util.NextPreviousService;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class LandingPageController implements Initializable {
    @FXML
    private VBox calendarViewVBox;

    @FXML
    private MenuButton calendarViewType;

    @FXML
    private Button nextDisplayButton;

    @FXML
    private Button previousDisplayButton;

    private User currentUser;

    private String currentDisplayedDate;

    private NextPreviousService currentNextPreviousService;


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

            LocalDate today = LocalDate.now();

            // Formater la date dans le format "yyyy-MM-dd"
            DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            currentDisplayedDate = today.format(formater);

            System.out.println("currentDate : "+currentDisplayedDate);

            nextDisplayButton.setOnAction(event ->{
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                // Convertir la chaîne en LocalDate
                LocalDate date = LocalDate.parse(currentDisplayedDate, formatter);

                // Ajouter une semaine à la date
                LocalDate datePlusOneWeek = date.plusWeeks(1);

                currentDisplayedDate = formatter.format(datePlusOneWeek);

                currentNextPreviousService.onNext(formatter.format(date));
            });

            previousDisplayButton.setOnAction(event -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                // Convertir la chaîne en LocalDate
                LocalDate date = LocalDate.parse(currentDisplayedDate, formatter);

                // Ajouter une semaine à la date
                LocalDate dateMinusOneWeek = date.minusWeeks(1);

                currentDisplayedDate = formatter.format(dateMinusOneWeek);

                currentNextPreviousService.onPrevious(formatter.format(date));
            });


            weeklyMenuItem.setOnAction(event -> {
                try {
                    FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/com/example/calendrierceri/weeklyCalendarView.fxml"));
                    Node weeklyView = loader2.load();
                    WeeklyCalendarViewController weeklyCalendarViewController = loader2.getController();
                    weeklyCalendarViewController.initializeWeeklyData(currentUser);
                    if (calendarViewVBox.getChildren().isEmpty()) {
                        calendarViewVBox.getChildren().add(weeklyView);
                        //calendarViewVBox.setFillWidth(true);
                    } else {
                        // Remplacer le contenu existant
                        calendarViewVBox.getChildren().set(0, weeklyView);
                    }
                    currentNextPreviousService = weeklyCalendarViewController;
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
                    //currentNextPreviousService = dailyCalendarViewController;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

    }

    public EventHandler<ActionEvent> nextDisplay() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Convertir la chaîne en LocalDate
        LocalDate date = LocalDate.parse(currentDisplayedDate, formatter);

        // Ajouter une semaine à la date
        LocalDate datePlusOneWeek = date.plusWeeks(1);

        currentDisplayedDate = formatter.format(datePlusOneWeek);

        return event -> {
            currentNextPreviousService.onNext(formatter.format(date));
        };
    }

    public EventHandler<ActionEvent> previousDisplay() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Convertir la chaîne en LocalDate
        LocalDate date = LocalDate.parse(currentDisplayedDate, formatter);

        // Ajouter une semaine à la date
        LocalDate dateMinusOneWeek = date.minusWeeks(1);

        currentDisplayedDate = formatter.format(dateMinusOneWeek);
        return event -> {
            currentNextPreviousService.onPrevious(formatter.format(date));
        };
    }
}
