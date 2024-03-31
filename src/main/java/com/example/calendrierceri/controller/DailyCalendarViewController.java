package com.example.calendrierceri.controller;

import com.example.calendrierceri.model.Event;
import com.example.calendrierceri.model.User;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.Locale;

import com.example.calendrierceri.util.FiltreService;

import static com.example.calendrierceri.controller.WeeklyCalendarViewController.extractHourIndexesOnCalendarView;
import static com.example.calendrierceri.controller.WeeklyCalendarViewController.mapResultSetToEvent;

public class DailyCalendarViewController implements Initializable, FiltreService {

    @FXML
    private GridPane dailyCalendarView;

    private Connection connection;
    private User currentUser;
    private List<Event> currentDayEvents = new ArrayList<>();

    private LocalDate currentDate;
    @FXML
    private Label currentDateLabel;

    private String currentFiltreCondition = "";
    private String currentFiltreValue = "";


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
        this.currentDate = date;
        updateDateLabel();
        loadEventsForDay(currentDate);
        addEventsToView();
    }

    // Ajoutez cette méthode pour mettre à jour le label de la date
    private void updateDateLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.FRANCE);
        String formattedDate = currentDate.format(formatter);
        currentDateLabel.setText(formattedDate);
    }

    private void loadEventsForDay(LocalDate date) {
        currentDayEvents.clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateString = date.format(formatter) + "%";

        String sqlQuery = "SELECT * FROM events WHERE dtstart LIKE ? AND (edt_id = ? OR edt_id = ?)";
        if (!this.currentFiltreCondition.isEmpty() && !this.currentFiltreValue.isEmpty()) {
            sqlQuery += " AND " + this.currentFiltreCondition + " = ?";
        }

        try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, dateString);
            statement.setInt(2, currentUser.getEdtPersonnelId());
            int edtId = currentUser.getEdtFormationId() != 0 ? currentUser.getEdtFormationId() : currentUser.getEdtProfId();
            statement.setInt(3, edtId);
            if (!this.currentFiltreCondition.isEmpty() && !this.currentFiltreValue.isEmpty()) {
                statement.setString(4, this.currentFiltreValue);
            }

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Event event = mapResultSetToEvent(resultSet);
                currentDayEvents.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Gérer l'exception correctement
        }
        addEventsToView();
    }


    private void addEventsToView() {
        dailyCalendarView.getChildren().clear();
        for (Event event : currentDayEvents) {
            Label eventLabel = createLabelFromEvent(event);
            int[] eventTimeIndexes = extractHourIndexesOnCalendarView(event);

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
        // Appliquer un style spécifique pour les exams
        if ("Evaluation".equals(event.getType())) {
            label.setStyle("-fx-background-color: red; -fx-border-color: red; -fx-padding: 5px;");
        } else {
            label.setStyle("-fx-background-color: #B0E0E6; -fx-border-color: #4682B4; -fx-padding: 5px;");
        }
        return label;
    }


    @Override
    public void onSalleFiltre(String searchDate, String filtreValue, int edtId, int personalEdtId) {
        this.currentFiltreCondition = "salle";
        this.currentFiltreValue = filtreValue;
        loadEventsForDay(this.currentDate);
    }

    @Override
    public void onTypeFiltre(String searchDate, String filtreValue, int edtId, int personalEdtId) {
        this.currentFiltreCondition = "type";
        this.currentFiltreValue = filtreValue;
        loadEventsForDay(this.currentDate);
    }

    @Override
    public void onMatiereFiltre(String searchDate, String filtreValue, int edtId, int personalEdtId) {
        this.currentFiltreCondition = "matiere";
        this.currentFiltreValue = filtreValue;
        loadEventsForDay(this.currentDate);
    }
}
