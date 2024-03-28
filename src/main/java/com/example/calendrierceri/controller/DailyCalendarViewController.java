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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DailyCalendarViewController implements Initializable {

    @FXML
    private GridPane dailyCalendarView;

    private Connection connection;

    private User currentUser;
    private List<Event> currentDayEvents = new ArrayList<>();

    public void setCurrentUser(User user){
        this.currentUser = user;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            connection = getDbConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void initializeDailyData(User user, LocalDate date) {
        currentUser = user;

        // Formater la date dans le format "yyyy-MM-dd"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateFormatted = date.format(formatter);

        if (currentUser.getRole().equals("Etudiant")) {
            mapDailyInfo(dateFormatted, currentUser.getEdtPersonnelId(), currentUser.getEdtFormationId());
        } else {
            mapDailyInfo(dateFormatted, currentUser.getEdtPersonnelId(), currentUser.getEdtProfId());
        }

        addEventsToView(currentDayEvents);
    }

    public void addEventsToView(List<Event> events) {
        for (Event event : events) {
            Label currentEventNode = createLabelFromEvent(event);

            // Ajouter le nœud à la grille
            dailyCalendarView.add(currentEventNode, 0, 0); // Vous pouvez ajuster la position selon vos besoins
        }
    }

    public void mapDailyInfo(String searchDate, int edtId, int edtExtraId) {
        if (!currentDayEvents.isEmpty())
            currentDayEvents.clear();

        String requeteSql = "SELECT * FROM events WHERE DATE(dtstart) = ? AND edt_id = ?";
        if (edtExtraId != 0) {
            requeteSql = "SELECT * FROM events WHERE DATE(dtstart) = ? AND (edt_id = ? OR edt_id = ?)";
        }

        try (PreparedStatement statement = connection.prepareStatement(requeteSql)) {
            statement.setString(1, searchDate);
            statement.setInt(2, edtId);

            if (edtExtraId != 0) {
                statement.setInt(3, edtExtraId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Event event = mapResultSetToEvent(resultSet);
                    currentDayEvents.add(event);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Event mapResultSetToEvent(ResultSet resultSet) throws SQLException {
        Event event = new Event();
        event.setEventId(resultSet.getInt("eventId"));
        event.setDtstart(resultSet.getString("dtstart"));
        event.setDtend(resultSet.getString("dtend"));
        event.setMatiere(resultSet.getString("matiere"));
        event.setEnseignant(resultSet.getString("enseignant"));
        event.setTd(resultSet.getString("td"));
        event.setSalle(resultSet.getString("salle"));
        event.setType(resultSet.getString("type"));
        event.setEdtId(resultSet.getInt("edtId"));
        return event;
    }


    public static Label createLabelFromEvent(Event event) {
        // Ici, vous ajustez la création du Label en fonction de ce que vous souhaitez afficher
        String text = event.getMatiere() + " - " + event.getType() + " en salle " + event.getSalle();
        return new Label(text); // Cela suppose que Label a un constructeur qui accepte une String
    }


    public static Connection getDbConnection() throws SQLException {
        // Remplacez les valeurs par celles de votre base de données
        String url = "jdbc:mysql://localhost:3306/edt";
        String username = "root";
        String password = "Smail@10";

        return DriverManager.getConnection(url, username, password);
    }
}
