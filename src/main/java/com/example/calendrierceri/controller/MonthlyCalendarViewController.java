package com.example.calendrierceri.controller;

import com.example.calendrierceri.model.Event;
import com.example.calendrierceri.model.User;
import com.example.calendrierceri.util.EdtIdFinder;
import com.example.calendrierceri.util.FiltreService;
import com.example.calendrierceri.util.NextPreviousService;
import com.example.calendrierceri.util.SearchService;
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

public class MonthlyCalendarViewController implements Initializable, NextPreviousService, FiltreService, SearchService {

    @FXML
    private GridPane monthlyCalendarView;

    private Connection connection;
    private User currentUser;

    private String currentSearchDate;

    private String currentSearchValue;

    private String currentFiltreValue;

    private String currentFiltreCondition;

    private int currentEdtId;

    private int currentPersonalEdtId;

    private EdtIdFinder edtIdFinder;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            connection = getDbConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void initializeMonthlyData(String searchDate,User user) throws SQLException {
        currentUser = user;
        edtIdFinder = new EdtIdFinder();
        if(currentUser.getRole().equals("Etudiant")){
            currentEdtId = currentUser.getEdtFormationId();
            currentPersonalEdtId=currentUser.getEdtPersonnelId();
        }else {
            currentEdtId = currentUser.getEdtProfId();
            currentPersonalEdtId=currentUser.getEdtPersonnelId();
        }
        addMonthDaysToView(searchDate,"","", currentEdtId, currentPersonalEdtId,"");
    }

    public String formatDateTimeString(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateString = formatter.format(date);
        return dateString + " 00:00:00";
    }

    public String buildEventCountQuery(boolean hasExtraId, String condition) {
        if (hasExtraId) {
            return "SELECT count(*) FROM events WHERE dtstart BETWEEN ? AND ? AND (edt_id = ? OR edt_id = ?)" + condition;
        } else {
            return "SELECT count(*) FROM events WHERE dtstart BETWEEN ? AND ? AND edt_id = ?" + condition;
        }
    }

    public int countMonthDayEvent(String searchDate,String filtreValue, int edtId, int edtExtraId, String condition) {
        String debutJourString = formatDateTimeString(LocalDate.parse(searchDate));
        String finJourString = debutJourString.replace("00:00:00", "23:59:59");

        String requeteSql = buildEventCountQuery(edtExtraId != 0, condition);
        try {
            PreparedStatement statement = connection.prepareStatement(requeteSql);
            statement.setString(1, debutJourString);
            statement.setString(2, finJourString);
            statement.setInt(3, edtId);
            if (edtExtraId != 0) {
                if(condition.isEmpty()||condition==null){
                    statement.setInt(4, edtExtraId);
                }else {
                    statement.setInt(4, edtExtraId);
                    statement.setString(5, filtreValue);
                }
            }else if (!condition.isEmpty()|| condition!=null && edtExtraId != 0) {
                statement.setString(4, filtreValue);
            }
            // Si nécessaire, ajoutez le paramètre condition ici
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'exécution de la requête", e);
        }
        return 0;
    }

    public void addMonthDaysToView(String searchDate, String filtreValue,String searchValue, int edtId, int personalEdtId, String condition) {
        clearMonthlyView();
        updateCurrentData(searchDate,filtreValue,searchValue,edtId,personalEdtId,condition);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(searchDate, formatter);
        LocalDate premierJourMois = date.withDayOfMonth(1);
        DayOfWeek premierJourSemaine = premierJourMois.getDayOfWeek();
        int numeroJour = 1;
        int ligne = 1;
        int colonne = premierJourSemaine.getValue() % 7 - 1;
        if (colonne == -1) {
            colonne = 6;
        }

        while (numeroJour <= date.lengthOfMonth()) {
            LocalDate currentDay = date.withDayOfMonth(numeroJour);
            int nbrOfEventsOnCurrentDate = countMonthDayEvent(currentDay.toString(),filtreValue, edtId, personalEdtId, condition);

            StackPane stackPane = new StackPane();
            Label labelHautDroite = new Label(String.valueOf(numeroJour));
            labelHautDroite.setStyle("-fx-font-weight: bold; -fx-padding: 5px");
            stackPane.getChildren().add(labelHautDroite);
            StackPane.setAlignment(labelHautDroite, Pos.TOP_RIGHT);

            Label labelBasGauche = new Label(String.valueOf(nbrOfEventsOnCurrentDate) + " evenements");
            labelBasGauche.setStyle("-fx-font-size: 10pt; -fx-text-fill: black; -fx-font-family: Arial; -fx-background-color: #B0E0E6; -fx-border-color: #4682B4; -fx-padding: 5px;");
            stackPane.getChildren().add(labelBasGauche);
            StackPane.setAlignment(labelBasGauche, Pos.BOTTOM_LEFT);

            monthlyCalendarView.add(stackPane, colonne, ligne);

            numeroJour++;
            colonne = (colonne + 1) % 7;
            if (colonne == 0) {
                ligne++;
            }
        }
    }
    public void updateCurrentData(String searchDate,String filreValue,String searchValue,int edtId,int personalEdtId, String filtreCondition){
        currentSearchDate=searchDate;
        currentFiltreValue=filreValue;
        currentSearchValue=searchValue;
        currentEdtId=edtId;
        currentPersonalEdtId=personalEdtId;
        currentFiltreCondition=filtreCondition;
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
        String url = "jdbc:mysql://localhost:3306/edt";
        String username = "root";
        String password = "Smail@10";

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

        addMonthDaysToView(datePlusOneMonthString,currentFiltreValue,currentSearchValue,currentEdtId,currentPersonalEdtId,currentFiltreCondition);

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

        addMonthDaysToView(dateMinusOneMonthString,currentFiltreValue,currentSearchValue,currentEdtId,currentPersonalEdtId,currentFiltreCondition);

        return dateMinusOneMonthString;
    }

    @Override
    public void onSalleFiltre(String searchDate, String filtreValue, int edtId, int personalEdtId) {
        addMonthDaysToView(searchDate, filtreValue,"", edtId, personalEdtId, " AND salle = ?");
    }

    @Override
    public void onTypeFiltre(String searchDate, String filtreValue, int edtId, int personalEdtId) {
        addMonthDaysToView(searchDate, filtreValue,"", edtId, personalEdtId, " AND type = ?");
    }

    @Override
    public void onMatiereFiltre(String searchDate, String filtreValue, int edtId, int personalEdtId) {
        addMonthDaysToView(searchDate, filtreValue,"", edtId, personalEdtId, " AND matiere = ?");
    }

    @Override
    public void onSpecialitySearch(String searchDate, String searchValue) {
        int edtId = edtIdFinder.getEdtIdFromSearchValue(searchValue);
        addMonthDaysToView(searchDate,"",searchValue,edtId,0,"");
    }

    @Override
    public void onTeacherSearch(String searchDate, String searchValue) {
        int edtId = edtIdFinder.getEdtIdFromSearchValue(searchValue);
        addMonthDaysToView(searchDate,"",searchValue,edtId,0,"");
    }

    @Override
    public void onClassSearch(String searchDate, String searchValue) {
        int edtId = edtIdFinder.getEdtIdFromSearchValue(searchValue);
        addMonthDaysToView(searchDate,"",searchValue,edtId,0,"");
    }
}