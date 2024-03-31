package com.example.calendrierceri.controller;

import com.example.calendrierceri.model.Event;
import com.example.calendrierceri.model.User;
import com.example.calendrierceri.util.FiltreService;
import com.example.calendrierceri.util.NextPreviousService;
import com.example.calendrierceri.util.SearchService;
import com.example.calendrierceri.util.EdtIdFinder;
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

import static com.example.calendrierceri.controller.WeeklyCalendarViewController.getDbConnection;
import static com.example.calendrierceri.controller.WeeklyCalendarViewController.mapResultSetToEvent;

public class DailyCalendarViewController implements Initializable, FiltreService, NextPreviousService, SearchService {

    @FXML
    private GridPane dailyCalendarView;

    private EdtIdFinder edtIdFinder;
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
            connection = getDbConnection();
            edtIdFinder = new EdtIdFinder();
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
                    Event event = mapResultSetToEvent(resultSet); // Assurez-vous que cette méthode est accessible
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

    private void loadEventsForDayWithEdtId(LocalDate date, int edtId) {
        // Nettoyer la vue actuelle
        dailyCalendarView.getChildren().clear();
        currentDayEvents.clear();

        // Charger les événements pour le jour spécifié en fonction de l'ID de EDT
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateString = date.format(formatter);

        String sqlQuery = "SELECT * FROM events WHERE dtstart LIKE ? AND edt_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, dateString + "%");
            statement.setInt(2, edtId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Event event = mapResultSetToEvent(resultSet);
                    currentDayEvents.add(event);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        addEventsToView();
        // Mettre à jour l'affichage de la date
        currentDate = date;
        updateDateLabel();
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

    @Override
    public void onSpecialitySearch(String searchDate, String searchValue) {
        int edtId = edtIdFinder.getEdtIdFromSearchValue(searchValue);
        if(edtId != -1) {
            loadEventsForDayWithEdtId(LocalDate.parse(searchDate), edtId);
        } else {
            System.out.println("Aucun EDT trouvé pour la spécialité: " + searchValue);
            // Gérer le cas où aucune spécialité n'est trouvée
        }
    }

    @Override
    public void onTeacherSearch(String searchDate, String searchValue) {
        // Recherche de l'ID de EDT basé sur le nom de l'enseignant
        int edtId = edtIdFinder.getEdtIdFromSearchValue(searchValue); // Cette méthode doit être adaptée pour rechercher par enseignant
        if (edtId != -1) {
            loadEventsForDayWithEdtId(LocalDate.parse(searchDate), edtId);
        } else {
            System.out.println("Aucun EDT trouvé pour l'enseignant: " + searchValue);
            // Gérer le cas où aucun enseignant n'est trouvé
        }
    }

    @Override
    public void onClassSearch(String searchDate, String searchValue) {
        // Recherche de l'ID de EDT basé sur le nom de la classe
        int edtId = edtIdFinder.getEdtIdFromSearchValue(searchValue); // Cette méthode doit être adaptée pour rechercher par classe
        if (edtId != -1) {
            loadEventsForDayWithEdtId(LocalDate.parse(searchDate), edtId);
        } else {
            System.out.println("Aucun EDT trouvé pour la classe: " + searchValue);
            // Gérer le cas où aucune classe n'est trouvée
        }
    }


    // Ajoutez les méthodes pour le jour suivant et précédent
    @Override
    public String onNext(String searchDate) {
        LocalDate date = LocalDate.parse(searchDate).plusDays(1);
        initializeDailyData(currentUser, date);
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Override
    public String onPrevious(String searchDate) {
        LocalDate date = LocalDate.parse(searchDate).minusDays(1);
        initializeDailyData(currentUser, date);
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }


}