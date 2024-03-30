package com.example.calendrierceri.controller;

import com.example.calendrierceri.model.Event;
import com.example.calendrierceri.model.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.ArrayList;

public class DailyCalendarViewController implements Initializable {

    @FXML
    private GridPane dailyCalendarView;

    private Connection connection;
    private User currentUser;
    private List<Event> currentDayEvents = new ArrayList<>();

    private LocalDate currentDate;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            connection = WeeklyCalendarViewController.getDbConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void initializeDailyData(User user, LocalDate date) {
        this.currentUser = user;
        this.currentDate = date; // Initialisez currentDate ici
        loadEventsForDay(currentDate);
        addEventsToView();
    }

    private void loadEventsForDay(LocalDate date) {
        currentDayEvents.clear(); // Clear existing events
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateString = date.format(formatter) + " %"; // Wildcard for LIKE clause

        String sqlQuery = "SELECT * FROM events WHERE dtstart LIKE ? AND (edt_id = ? OR edt_id = ?)";

        try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, dateString);
            statement.setInt(2, currentUser.getEdtPersonnelId());
            // Assuming 0 indicates that the ID is not set or is invalid.
            int edtId = currentUser.getEdtFormationId() != 0 ? currentUser.getEdtFormationId() : currentUser.getEdtProfId();
            statement.setInt(3, edtId);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Event event = WeeklyCalendarViewController.mapResultSetToEvent(resultSet);
                currentDayEvents.add(event);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addEventsToView() {
        for (Event event : currentDayEvents) {
            Label eventLabel = WeeklyCalendarViewController.createLabelFromEvent(event);
            int[] eventTimeIndexes = WeeklyCalendarViewController.extractHourIndexesOnCalendarView(event);

            dailyCalendarView.add(eventLabel, 0, eventTimeIndexes[0], 1, eventTimeIndexes[1] - eventTimeIndexes[0]);
        }
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
