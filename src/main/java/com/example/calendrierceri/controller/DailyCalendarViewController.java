package com.example.calendrierceri.controller;

import com.example.calendrierceri.model.Event;
import com.example.calendrierceri.model.User;
import com.example.calendrierceri.util.FiltreService; // Assurez-vous que cette importation est correcte
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class DailyCalendarViewController implements Initializable, FiltreService {

    @FXML
    private GridPane dailyCalendarView;
    private Connection connection;
    private User currentUser;
    private List<Event> currentDayEvents = new ArrayList<>();
    private LocalDate currentDate;
    @FXML
    private Label currentDateLabel;

    // Attributs pour le filtrage
    private String currentFiltreCondition = "";
    private String currentFiltreValue = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            connection = WeeklyCalendarViewController.getDbConnection(); // Assurez-vous que cette méthode est accessible
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void initializeDailyData(User user, LocalDate date) {
        this.currentUser = user;
        this.currentDate = date;
        updateDateLabel();
        loadEventsForDay(currentDate);
        addEventsToView();
    }

    private void updateDateLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.FRANCE);
        String formattedDate = currentDate.format(formatter);
        currentDateLabel.setText(formattedDate);
    }

    private void loadEventsForDay(LocalDate date) {
        currentDayEvents.clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateString = date.format(formatter);

        String sqlQuery = "SELECT * FROM events WHERE dtstart LIKE ? AND (edt_id = ? OR edt_id = ?)" + currentFiltreCondition;

        try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, dateString + "%");
            statement.setInt(2, currentUser.getEdtPersonnelId());
            statement.setInt(3, currentUser.getEdtFormationId());
            if (!currentFiltreCondition.isEmpty()) {
                statement.setString(4, currentFiltreValue);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Event event = WeeklyCalendarViewController.mapResultSetToEvent(resultSet); // Assurez-vous que cette méthode est accessible
                    currentDayEvents.add(event);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addEventsToView() {
        dailyCalendarView.getChildren().clear();
        for (Event event : currentDayEvents) {
            Label eventLabel = createLabelFromEvent(event);
            int[] eventTimeIndexes = WeeklyCalendarViewController.extractHourIndexesOnCalendarView(event); // Assurez-vous que cette méthode est accessible

            dailyCalendarView.add(eventLabel, 0, eventTimeIndexes[0], 1, eventTimeIndexes[1] - eventTimeIndexes[0]);
        }
    }

    private Label createLabelFromEvent(Event event) {
        Label label = new Label();
        String text = event.getMatiere() + "\n" +
                "Enseignant: " + event.getEnseignant() + "\n" +
                "Salle: " + event.getSalle() + "\n" +
                "Type: " + event.getType() + "\n" +
                "Promotions: " + event.getTd();
        label.setText(text);

        label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        label.setTextFill(Color.BLACK);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        if ("Evaluation".equals(event.getType())) {
            label.setStyle("-fx-background-color: red; -fx-border-color: red; -fx-padding: 5px;");
        } else {
            label.setStyle("-fx-background-color: #B0E0E6; -fx-border-color: #4682B4; -fx-padding: 5px;");
        }
        return label;
    }

    @Override
    public void onSalleFiltre(String searchDate, String filtreValue, int edtId, int personalEdtId) {
        this.currentFiltreCondition = " AND salle = ?";
        this.currentFiltreValue = filtreValue;
        loadEventsForDay(currentDate);
        addEventsToView();
    }

    @Override
    public void onTypeFiltre(String searchDate, String filtreValue, int edtId, int personalEdtId) {
        this.currentFiltreCondition = " AND type = ?";
        this.currentFiltreValue = filtreValue;
        loadEventsForDay(currentDate);
        addEventsToView();
    }

    @Override
    public void onMatiereFiltre(String searchDate, String filtreValue, int edtId, int personalEdtId) {
        this.currentFiltreCondition = " AND matiere = ?";
        this.currentFiltreValue = filtreValue;
        loadEventsForDay(currentDate);
        addEventsToView();
    }

    // Ajoutez les méthodes pour le jour suivant et précédent
    @FXML
    public void goToNextDay() {
        currentDate = currentDate.plusDays(1);
        loadEventsForDay(currentDate);
        addEventsToView();
    }

    @FXML
    public void goToPreviousDay() {
        currentDate = currentDate.minusDays(1);
        loadEventsForDay(currentDate);
        addEventsToView();
    }
}
