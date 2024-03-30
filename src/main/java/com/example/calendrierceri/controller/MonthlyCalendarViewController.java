package com.example.calendrierceri.controller;

import com.example.calendrierceri.model.Event;
import com.example.calendrierceri.model.User;
import com.example.calendrierceri.util.NextPreviousService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class MonthlyCalendarViewController implements Initializable, NextPreviousService {

    @FXML
    private GridPane monthlyCalendarView;

    private Connection connection;
    private User currentUser;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            connection = getDbConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void initializeMonthlyData(String searchDate,User user){
        currentUser = user;
        addMonthDaysToView(searchDate);
//        if(currentUser.getRole().equals("Etudiant")){
//            mapWeekInfo(searchDate, currentUser.getEdtPersonnelId(), currentUser.getEdtFormationId());
//        }else {
//            mapWeekInfo(searchDate,currentUser.getEdtPersonnelId(), currentUser.getEdtProfId());
//        }
//        for(Event event : currentMonthEvents){
//            System.out.println("Event from month List : " + event);
//        }
    }


    public void addEventsToView(String searchDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(searchDate, formatter);

        Month monthLength = date.getMonth();
    }

    public int countMonthDayEvent(String searchDate,int edtId,int edtExtraId){

        String searchDateDouble = searchDate;

        String debutJourString = searchDate.concat(" 00:00:00");
        String finJourString = searchDateDouble.concat(" 23:59:59");

        String requeteSql;
        PreparedStatement statement;
        if (edtExtraId == 0) {
            requeteSql = "SELECT count(*) FROM events WHERE dtstart BETWEEN ? AND ? AND edt_id = ?";
        } else {
            requeteSql = "SELECT count(*) FROM events WHERE dtstart BETWEEN ? AND ? AND (edt_id = ? OR edt_id = ?)";
        }

        try {
            statement = connection.prepareStatement(requeteSql);
            statement.setString(1, debutJourString);
            statement.setString(2, finJourString);
            statement.setInt(3, edtId);

            // Si edtExtraId n'est pas égal à zéro, nous avons une quatrième condition
            if (edtExtraId != 0) {
                statement.setInt(4, edtExtraId);
            }

            // Exécuter la requête ici
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
        throw new RuntimeException("Erreur lors de l'exécution de la requête", e);
    }
    return 0;
}

    public void addMonthDaysToView(String searchDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(searchDate, formatter);

        LocalDate premierJourMois = date.withDayOfMonth(1);
        DayOfWeek premierJourSemaine = premierJourMois.getDayOfWeek();
        int numeroJour = 1;

        int ligne = 1;
        int colonne = premierJourSemaine.getValue() % 7 - 1 ;
        if(colonne == -1){
            colonne = 6;
        }

        while (numeroJour <= date.lengthOfMonth()) {

            LocalDate currentDay = date.withDayOfMonth(numeroJour);
            String currentDayString = formatter.format(currentDay);

            int nbrOfEventsOnCurrentDate ;

            if(currentUser.getRole().equals("Etudiant")){
                nbrOfEventsOnCurrentDate = countMonthDayEvent(currentDayString, currentUser.getEdtPersonnelId(), currentUser.getEdtFormationId());
            }else {
                nbrOfEventsOnCurrentDate = countMonthDayEvent(currentDayString,currentUser.getEdtPersonnelId(), currentUser.getEdtProfId());
            }
            StackPane stackPane = new StackPane();
//            stackPane.setAlignment(Pos.TOP_RIGHT);
//            stackPane.getChildren().add(new Label(String.valueOf(numeroJour)));
//
//            monthlyCalendarView.add(stackPane, colonne, ligne);

            // Ajoutez le label existant en haut à droite
            Label labelHautDroite = new Label(String.valueOf(numeroJour));
            labelHautDroite.setStyle("-fx-font-weight: bold; -fx-padding: 5px");
            stackPane.getChildren().add(labelHautDroite);
            StackPane.setAlignment(labelHautDroite, Pos.TOP_RIGHT);

            // Ajoutez le nouveau label en bas à gauche
            Label labelBasGauche = new Label(String.valueOf(nbrOfEventsOnCurrentDate)+" evenements");
            labelBasGauche.setStyle("-fx-font-size: 10pt; -fx-text-fill: black; -fx-font-family: Arial; -fx-background-color: #B0E0E6; -fx-border-color: #4682B4; -fx-padding: 5px;");
            stackPane.getChildren().add(labelBasGauche);
            StackPane.setAlignment(labelBasGauche, Pos.BOTTOM_LEFT);

            monthlyCalendarView.add(stackPane, colonne, ligne);

            numeroJour++;

            // Passer à la ligne suivante et réinitialiser la colonne si nécessaire
            colonne = (colonne + 1) % 7;
            if (colonne == 0) {
                ligne++;
            }
        }
    }

    public int extractDayIndex(Event event){
        String dateString = event.getDtstart();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Analyser la chaîne de caractères de date en un objet LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);

        // Extraire la date pour obtenir l'index du jour de la semaine
        LocalDate date = dateTime.toLocalDate();

        return date.getDayOfWeek().getValue() - 1;
    }

    public int extractWeekIndex(Event event){
        String dateString = event.getDtstart();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Analyser la chaîne de caractères de date en un objet LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);

        // Extraire la date pour obtenir l'index du jour de la semaine
        LocalDate date = dateTime.toLocalDate();

        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        return date.get(weekFields.weekOfMonth());
    }

    public void clearMonthlyView(){
        List<Node> nodesToRemove = new ArrayList<>();

        // Parcourir les enfants du GridPane
        for (Node node : monthlyCalendarView.getChildren()) {
            // Vérifier si le nœud est une instance de StackPane
            if (node instanceof StackPane) {
                // Ajouter le nœud à la liste des nœuds à supprimer
                nodesToRemove.add(node);
            }
        }
        // Supprimer les nœuds de la liste temporaire
        monthlyCalendarView.getChildren().removeAll(nodesToRemove);
    }

    public static Connection getDbConnection() throws SQLException {
        // Remplacez les valeurs par celles de votre base de données
        String url = "jdbc:mysql://localhost:3306/edt_ceri";
        String username = "root";
        String password = "root";

        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public String onNext(String searchDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Convertir la chaîne en LocalDate
        LocalDate date = LocalDate.parse(searchDate, formatter);

        // Ajouter une semaine à la date
        LocalDate datePlusOneMonth = date.plusMonths(1);

        String datePlusOneMonthString = formatter.format(datePlusOneMonth);

        clearMonthlyView();

        addMonthDaysToView(datePlusOneMonthString);

        return datePlusOneMonthString;
    }

    @Override
    public String onPrevious(String searchDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Convertir la chaîne en LocalDate
        LocalDate date = LocalDate.parse(searchDate, formatter);

        // Ajouter une semaine à la date
        LocalDate dateMinusOneMonth = date.minusMonths(1);

        String dateMinusOneMonthString = formatter.format(dateMinusOneMonth);

        clearMonthlyView();

        addMonthDaysToView(dateMinusOneMonthString);

        return dateMinusOneMonthString;
    }
}