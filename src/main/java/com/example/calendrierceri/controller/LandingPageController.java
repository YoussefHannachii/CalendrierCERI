package com.example.calendrierceri.controller;

import com.example.calendrierceri.model.User;
import com.example.calendrierceri.util.NextPreviousService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
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

    @FXML
    private Label labelEdtInfo;

    @FXML
    private Label monthDisplayed;

    private User currentUser;

    private String currentDisplayedDate;

    private NextPreviousService currentNextPreviousService;


    public void setCurrentUser(User user){
        this.currentUser=user;
        labelEdtInfo.setText(currentUser.getPrenom() + " " + currentUser.getNom() +" calendar");
        labelEdtInfo.setStyle("-fx-font-family: Arial; -fx-font-weight: bold; -fx-font-size: 12px");
    }

    public void updateMonthDisplayed(String currentDate){
        // Formatter pour analyser la chaîne de date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Analyser la chaîne de date en objet LocalDate
        LocalDate date = LocalDate.parse(currentDate, formatter);

        // Extraire le nom du mois à partir de l'objet LocalDate
        String nomDuMois = date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());

        String annee = String.valueOf(date.getYear());;

        monthDisplayed.setText(nomDuMois+"/"+annee);
        monthDisplayed.setStyle("-fx-font-family: Arial; -fx-font-weight: bold; -fx-font-size: 12px");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

            calendarViewType.getItems().clear();

            MenuItem weeklyMenuItem = new MenuItem("Weekly");
            MenuItem dailyMenuItem = new MenuItem("Daily");
            MenuItem monthlyMenuItem = new MenuItem("Monthly");


            // Ajouter les éléments de menu au MenuButton
            calendarViewType.getItems().addAll(dailyMenuItem, weeklyMenuItem, monthlyMenuItem);

            LocalDate today = LocalDate.now();


            // Formater la date dans le format "yyyy-MM-dd"
            DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            currentDisplayedDate = today.format(formater);

            updateMonthDisplayed(currentDisplayedDate);

            nextDisplayButton.setOnAction(event ->{
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                // Convertir la chaîne en LocalDate
                LocalDate date = LocalDate.parse(currentDisplayedDate, formatter);

                currentDisplayedDate = currentNextPreviousService.onNext(formatter.format(date));

                updateMonthDisplayed(currentDisplayedDate);
            });

            previousDisplayButton.setOnAction(event -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                // Convertir la chaîne en LocalDate
                LocalDate date = LocalDate.parse(currentDisplayedDate, formatter);

                currentDisplayedDate = currentNextPreviousService.onPrevious(formatter.format(date));

                updateMonthDisplayed(currentDisplayedDate);
            });


            weeklyMenuItem.setOnAction(event -> {
                try {
                    FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/com/example/calendrierceri/weeklyCalendarView.fxml"));
                    Node weeklyView = loader2.load();
                    WeeklyCalendarViewController weeklyCalendarViewController = loader2.getController();
                    weeklyCalendarViewController.initializeWeeklyData(currentDisplayedDate,currentUser);
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

        monthlyMenuItem.setOnAction(event -> {
            try {
                FXMLLoader loaderMonthly = new FXMLLoader(getClass().getResource("/com/example/calendrierceri/monthlyCalendarView.fxml"));
                Node monthlyView = loaderMonthly.load();
                MonthlyCalendarViewController monthlyCalendarViewController = loaderMonthly.getController();
                monthlyCalendarViewController.initializeMonthlyData(currentDisplayedDate,currentUser);
                if (calendarViewVBox.getChildren().isEmpty()) {
                    calendarViewVBox.getChildren().add(monthlyView);
                } else {
                    // Remplacer le contenu existant
                    calendarViewVBox.getChildren().set(0, monthlyView);
                }
                currentNextPreviousService = monthlyCalendarViewController;
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
