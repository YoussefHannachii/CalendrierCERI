package com.example.calendrierceri.controller;

import com.example.calendrierceri.model.Event;
import com.example.calendrierceri.model.User;
import com.example.calendrierceri.util.EdtIdFinder;
import com.example.calendrierceri.util.FiltreService;
import com.example.calendrierceri.util.NextPreviousService;
import com.example.calendrierceri.util.SearchService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class WeeklyCalendarViewController implements Initializable, NextPreviousService, FiltreService, SearchService {

    @FXML
    private GridPane weeklyCalendarView;
    private Connection connection;
    private User currentUser;
    private List<Event> currentWeekEvents = new ArrayList<>();
    private String currentSearchDate;
    private String currentFiltreValue;
    private int currentPersonalEdtId;
    private int currentEdtId;
    private String currentFiltreCondition;
    private String currentSearchValur;

    private EdtIdFinder edtIdFinder;

    public void setCurrentUser(User user){
        this.currentUser=user;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            connection = getDbConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void initializeWeeklyData(String searchData,User user) throws SQLException {
        edtIdFinder = new EdtIdFinder();
        addDayInfoOnDayLabel(searchData);
        currentUser=user;
        if(currentUser.getRole().equals("Etudiant")){
            mapWeekInfo(searchData, currentUser.getEdtPersonnelId(), currentUser.getEdtFormationId());
            updateCurrentData(searchData,"","",currentUser.getEdtFormationId(),currentUser.getEdtPersonnelId(),"");
        }else {
            mapWeekInfo(searchData,currentUser.getEdtPersonnelId(), currentUser.getEdtProfId());
            updateCurrentData(searchData,"","",currentUser.getEdtProfId(),currentUser.getEdtPersonnelId(),"");
        }
        addEventsToView(currentWeekEvents);
    }

    public void addEventsToView(List<Event> events){
        for(Event event : events){
            ScrollPane currentEventNode = createLabelFromEvent(event);
            int dayIndex = extractDayIndexOnCalendarView(event);
            //rend un tableau avec l'index de debut et de fin du event
            int[] eventDurationIndexes = extractHourIndexesOnCalendarView(event);
            int eventStartIndex = eventDurationIndexes[0];
            int eventEndIndex = eventDurationIndexes[1];
            int eventDuration = eventEndIndex-eventStartIndex;

            if (eventDuration > 1) {
                weeklyCalendarView.setRowSpan(currentEventNode, eventDuration);
            }

            // Ajouter le nœud à la grille à l'index de la rangée spécifié
            weeklyCalendarView.add(currentEventNode, dayIndex, eventStartIndex);
        }
    }

    public void mapWeekInfo(String searchDate,int edtId,int edtExtraId){
        if (!currentWeekEvents.isEmpty())
            currentWeekEvents.clear();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(searchDate, formatter); // Conversion de la chaîne en LocalDate

        // Trouver le début de la semaine (lundi) à partir de la date donnée
        LocalDate debutSemaine = date.with(DayOfWeek.MONDAY);

        // Trouver la fin de la semaine (dimanche) à partir de la date donnée
        LocalDate finSemaine = date.with(DayOfWeek.SUNDAY);

        // Formater les dates en chaînes de caractères
        String debutSemaineString = debutSemaine.format(formatter);
        String finSemaineString = finSemaine.format(formatter);

        debutSemaineString = debutSemaineString.concat(" 00:00:00");
        finSemaineString = finSemaineString.concat(" 23:59:59");

        String requeteSql;
        PreparedStatement statement;
        if (edtExtraId == 0) {
            requeteSql = "SELECT * FROM events WHERE dtstart BETWEEN ? AND ? AND edt_id = ?";
        } else {
            requeteSql = "SELECT * FROM events WHERE dtstart BETWEEN ? AND ? AND (edt_id = ? OR edt_id = ?)";
        }

        try {
            statement = connection.prepareStatement(requeteSql);
            statement.setString(1, debutSemaineString);
            statement.setString(2, finSemaineString);
            statement.setInt(3, edtId);

            // Si edtExtraId n'est pas égal à zéro, nous avons une quatrième condition
            if (edtExtraId != 0) {
                statement.setInt(4, edtExtraId);
            }

            // Exécuter la requête ici
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Exécution de la requête
        try (ResultSet resultSet = statement.executeQuery()) {
            // Parcours des résultats
            while (resultSet.next()) {
                Event event = mapResultSetToEvent(resultSet);
                currentWeekEvents.add(event);
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }


    public static Event mapResultSetToEvent(ResultSet resultSet) throws SQLException {
        Event event = new Event();
        event.setEventId(resultSet.getInt("event_id"));
        event.setDtstart(resultSet.getString("dtstart"));
        event.setDtend(resultSet.getString("dtend"));
        String matiere = resultSet.getString("matiere");
        if (matiere != null) {
            event.setMatiere(matiere);
        } else {
            event.setMatiere("Aucune matière spécifiée");
        }

        // Vérification pour le champ "enseignant"
        String enseignant = resultSet.getString("enseignant");
        if (enseignant != null) {
            event.setEnseignant(enseignant);
        } else {
            event.setEnseignant("Aucun enseignant spécifié");
        }

        // Vérification pour le champ "td"
        String td = resultSet.getString("td");
        if (td != null) {
            event.setTd(td);
        } else {
            event.setTd("Aucun TD spécifié");
        }

        // Vérification pour le champ "salle"
        String salle = resultSet.getString("salle");
        if (salle != null) {
            event.setSalle(salle);
        } else {
            event.setSalle("Aucune salle spécifiée");
        }

        // Vérification pour le champ "type"
        String type = resultSet.getString("type");
        if (type != null) {
            event.setType(type);
        } else {
            event.setType("Aucun type spécifié");
        }
        event.setEdtId(resultSet.getInt("edt_id"));

        String description = resultSet.getString("event_description");
        if (description != null) {
            event.setDescription(description);
        } else {
            event.setDescription("Aucun type spécifié");
        }
        return event;
    }


    public static Connection getDbConnection() throws SQLException {
        // Remplacez les valeurs par celles de votre base de données
        String url = "jdbc:mysql://localhost:3306/edt_ceri";
        String username = "root";
        String password = "root";

        return DriverManager.getConnection(url, username, password);
    }

    public static int[] extractHourIndexesOnCalendarView(Event event){

        String dateStringDebut = event.getDtstart();
        String dateStringFin = event.getDtend();

        // Formater de date pour le format spécifié
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Analyser les chaînes de caractères de date de début et de fin en objets LocalDateTime
        LocalDateTime dateTimeDebut = LocalDateTime.parse(dateStringDebut, formatter);
        LocalDateTime dateTimeFin = LocalDateTime.parse(dateStringFin, formatter);

        // Extraire l'heure de début et de fin
        LocalTime heureDebut = dateTimeDebut.toLocalTime();
        LocalTime heureFin = dateTimeFin.toLocalTime();

        // Calculer les index correspondants dans le tableau
        int indexDebut = calculerIndexHoraire(heureDebut);
        int indexFin = calculerIndexHoraire(heureFin);

        return new int[] { indexDebut, indexFin };
    }

    public int extractDayIndexOnCalendarView(Event event){
        String dateString = event.getDtstart();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Analyser la chaîne de caractères de date en un objet LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);

        // Extraire la date pour obtenir l'index du jour de la semaine
        LocalDate date = dateTime.toLocalDate();

        // Obtenir l'index du jour de la semaine (1 pour Lundi, 2 pour Mardi, ..., 7 pour Dimanche)
        int indexDayOfTheWeek = date.getDayOfWeek().getValue();

        // Afficher l'index du jour de la semaine
        System.out.println("Index du jour de la semaine : " + indexDayOfTheWeek);

        return indexDayOfTheWeek;
    }

    private static int calculerIndexHoraire(LocalTime heure) {
        // L'index de l'heure 6:00 est 1, 7:00 est 2, ..., 18:00 est 13
        return heure.getHour() - 5; // Parce que 6:00 correspond à l'index 1
    }

    public static ScrollPane createLabelFromEvent(Event event) {
        String[] enseignantParts = event.getEnseignant().split(" ");
        String nom = enseignantParts.length > 1 ? enseignantParts[1] : "";
        String prenom = enseignantParts.length > 0 ? enseignantParts[0] : "";
        String email = nom.toLowerCase() + "." + prenom.toLowerCase() + "@alumni.univ-avignon.fr";

        Hyperlink enseignantLink = new Hyperlink(event.getEnseignant());
        enseignantLink.setOnAction(e -> openMailTo(email));

        Text text = new Text(event.getMatiere() + "\n" +
                "Enseignant: " + enseignantLink + "\n" +
                "Salle: " + event.getSalle() + "\n" +
                "Type: " + event.getType() + "\n" +
                "Promotions: " + event.getTd());

        // Appliquer un style au Text
        text.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        text.setFill(Color.BLACK);
        StackPane textContainer = new StackPane();
        textContainer.setStyle("-fx-background-color: #B0E0E6; " +
                "-fx-padding: 5px;");
        textContainer.getChildren().add(text);

        // Créer un ScrollPane pour contenir le Text
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(textContainer);

        // Autoriser le défilement vertical si nécessaire
        scrollPane.setFitToHeight(true);

        // Appliquer un style au ScrollPane
        if (event.getType().equals("Evaluation")) {
            scrollPane.setStyle("-fx-background-color: #FF5B5B; " +
                    "-fx-border-color: red; " +
                    "-fx-padding: 5px;");
        }else if(event.getType().equals("Perso")){
            Text textEventPersonnal = new Text(
                    "Type: " + event.getType() + "\n" +
                    "Desciption: " + event.getDescription());
            textEventPersonnal.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            textEventPersonnal.setFill(Color.BLACK);
            StackPane textEventPersonnalContainer = new StackPane();
            textEventPersonnalContainer.setStyle("-fx-background-color: #F4FF51; " +
                    "-fx-padding: 5px;");
            textEventPersonnalContainer.getChildren().add(textEventPersonnal);
            scrollPane.setContent(textEventPersonnalContainer);
            // Autoriser le défilement vertical si nécessaire
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: #F4FF51; " +
                    "-fx-border-color: black; " +
                    "-fx-padding: 5px;");
        } else {
            scrollPane.setStyle("-fx-background-color: #B0E0E6; " +
                    "-fx-border-color: #4682B4; " +
                    "-fx-padding: 5px;");
        }

        return scrollPane;
    }

    public void updateCurrentData(String searchDate,String filreValue,String searchValue ,int edtId,int personalEdtId, String filtreCondition){
        currentSearchDate=searchDate;
        currentSearchValur = searchValue;
        currentFiltreValue=filreValue;
        currentEdtId=edtId;
        currentPersonalEdtId=personalEdtId;
        currentFiltreCondition=filtreCondition;
    }

    //Date en entrée c'est la date affichée sur le landing page
    //donc il faut chercher la semaine qui d'aprés
    @Override
    public String onNext(String searchDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Convertir la chaîne en LocalDate
        LocalDate date = LocalDate.parse(searchDate, formatter);

        // Ajouter une semaine à la date
        LocalDate datePlusOneWeek = date.plusWeeks(1);

        String datePlusOneWeekString = formatter.format(datePlusOneWeek);

        if(!currentSearchValur.isEmpty()){
            mapWeekInfo(datePlusOneWeekString,currentEdtId,0);
            addDayInfoOnDayLabel(datePlusOneWeekString);
            clearGridPane(weeklyCalendarView);
            addEventsToView(currentWeekEvents);
            return datePlusOneWeekString;
        }

        if(!currentFiltreCondition.isEmpty()){
            filtrerEvenements(datePlusOneWeekString,currentFiltreValue,currentEdtId,currentPersonalEdtId,currentFiltreCondition);
            addDayInfoOnDayLabel(datePlusOneWeekString);
            return datePlusOneWeekString;
        }

        if(currentUser.getRole().equals("Etudiant")){
            mapWeekInfo(datePlusOneWeekString, currentUser.getEdtPersonnelId(), currentUser.getEdtFormationId());
        }else {
            mapWeekInfo(datePlusOneWeekString,currentUser.getEdtPersonnelId(), currentUser.getEdtProfId());
        }
        clearGridPane(weeklyCalendarView);
        addEventsToView(currentWeekEvents);
        addDayInfoOnDayLabel(datePlusOneWeekString);
        return datePlusOneWeekString;
    }

    @Override
    public String onPrevious(String searchDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Convertir la chaîne en LocalDate
        LocalDate date = LocalDate.parse(searchDate, formatter);

        // Ajouter une semaine à la date
        LocalDate dateMinusOneWeek = date.minusWeeks(1);

        String dateMinusOneWeekString = formatter.format(dateMinusOneWeek);

        if(!currentSearchValur.isEmpty()){
            mapWeekInfo(dateMinusOneWeekString,currentEdtId,0);
            addDayInfoOnDayLabel(dateMinusOneWeekString);
            clearGridPane(weeklyCalendarView);
            addEventsToView(currentWeekEvents);
            return dateMinusOneWeekString;
        }

        if(!currentFiltreCondition.isEmpty()){
            filtrerEvenements(dateMinusOneWeekString,currentFiltreValue,currentEdtId,currentPersonalEdtId,currentFiltreCondition);
            addDayInfoOnDayLabel(dateMinusOneWeekString);
            return dateMinusOneWeekString;
        }

        if(currentUser.getRole().equals("Etudiant")){
            mapWeekInfo(dateMinusOneWeekString, currentUser.getEdtPersonnelId(), currentUser.getEdtFormationId());
        }else {
            mapWeekInfo(dateMinusOneWeekString,currentUser.getEdtPersonnelId(), currentUser.getEdtProfId());
        }
        clearGridPane(weeklyCalendarView);
        addEventsToView(currentWeekEvents);
        addDayInfoOnDayLabel(dateMinusOneWeekString);
        return dateMinusOneWeekString;
    }

    public static void clearGridPane(GridPane gridPane) {
        // Créer une liste temporaire pour stocker les nœuds à supprimer
        List<Node> nodesToRemove = new ArrayList<>();

        // Parcourir les enfants du GridPane
        for (Node node : gridPane.getChildren()) {
            // Vérifier si le nœud est une instance de ScrollPane
            if (node instanceof ScrollPane) {
                // Ajouter le nœud à la liste des nœuds à supprimer
                nodesToRemove.add(node);
            }
        }

        // Supprimer les nœuds de la liste temporaire
        gridPane.getChildren().removeAll(nodesToRemove);
    }

    public void addDayInfoOnDayLabel(String searchDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(searchDate, formatter); // Conversion de la chaîne en LocalDate
        // Créer un DateTimeFormatter pour formater les dates en "dd-MM"
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM");

        // Trouver le début de la semaine (lundi) à partir de la date donnée
        LocalDate debutSemaine = date.with(DayOfWeek.MONDAY);

        for (Node node : weeklyCalendarView.getChildren()) {
            LocalDate debutSemaineUtil = debutSemaine;
            if(node instanceof VBox){
                Integer colIndex = GridPane.getColumnIndex(node);
                    if(colIndex!=null && colIndex!=0)
                    {
                        VBox vbox = (VBox) node;
                        Label label = new Label(debutSemaineUtil.plusDays(colIndex-1).format(outputFormatter));
                        label.setStyle("-fx-font-weight:bold;");

                        if (vbox.getChildren().size() > 1) { // Vérifier si un nœud existe déjà à la deuxième position
                            vbox.getChildren().remove(1); // Supprimer le nœud existant à la deuxième position
                        }
                        vbox.getChildren().add(1, label); // Ajoute le label à la deuxième position dans le VBox
                    }
            }

        }

    }

    private void filtrerEvenements(String searchDate, String filtreValue, int edtId, int personalEdtId, String condition) {
        if (!currentWeekEvents.isEmpty())
            currentWeekEvents.clear();

        updateCurrentData(searchDate,filtreValue,"",edtId,personalEdtId,condition);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(searchDate, formatter); // Conversion de la chaîne en LocalDate

        // Trouver le début de la semaine (lundi) à partir de la date donnée
        LocalDate debutSemaine = date.with(DayOfWeek.MONDAY);

        // Trouver la fin de la semaine (dimanche) à partir de la date donnée
        LocalDate finSemaine = date.with(DayOfWeek.SUNDAY);

        // Formater les dates en chaînes de caractères
        String debutSemaineString = debutSemaine.format(formatter);
        String finSemaineString = finSemaine.format(formatter);

        debutSemaineString = debutSemaineString.concat(" 00:00:00");
        finSemaineString = finSemaineString.concat(" 23:59:59");

        String requeteSql;
        PreparedStatement statement;
        if (personalEdtId == 0) {
            requeteSql = "SELECT * FROM events WHERE dtstart BETWEEN ? AND ? AND edt_id = ? " + condition;
        } else {
            requeteSql = "SELECT * FROM events WHERE dtstart BETWEEN ? AND ? AND (edt_id = ? OR edt_id = ?) " + condition;
        }

        try {
            statement = connection.prepareStatement(requeteSql);
            statement.setString(1, debutSemaineString);
            statement.setString(2, finSemaineString);
            statement.setInt(3, edtId);
            statement.setString(4, filtreValue);

            // Si personalEdtId n'est pas égal à zéro, nous avons une quatrième condition
            if (personalEdtId != 0) {
                statement.setInt(4, personalEdtId);
                statement.setString(5, filtreValue);
            }

            // Exécuter la requête ici
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Exécution de la requête
        try (ResultSet resultSet = statement.executeQuery()) {
            // Parcours des résultats
            while (resultSet.next()) {
                Event event = mapResultSetToEvent(resultSet);
                currentWeekEvents.add(event);
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        clearGridPane(weeklyCalendarView);
        addEventsToView(currentWeekEvents);
    }


    @Override
    public void onSalleFiltre(String searchDate, String filtreValue, int edtId, int personalEdtId) {
        filtrerEvenements(searchDate, filtreValue, edtId, personalEdtId, " AND salle = ?");
    }

    @Override
    public void onTypeFiltre(String searchDate, String filtreValue, int edtId, int personalEdtId) {
        filtrerEvenements(searchDate, filtreValue, edtId, personalEdtId, " AND type = ?");
    }

    @Override
    public void onMatiereFiltre(String searchDate, String filtreValue, int edtId, int personalEdtId) {
        filtrerEvenements(searchDate, filtreValue, edtId, personalEdtId, " AND matiere = ?");
    }

    @Override
    public void onSpecialitySearch(String searchDate, String searchValue) {
        int edtId = edtIdFinder.getEdtIdFromSearchValue(searchValue);
        updateCurrentData(searchDate,"",searchValue,edtId,0,"");
        mapWeekInfo(searchDate,edtId,0);
        clearGridPane(weeklyCalendarView);
        addEventsToView(currentWeekEvents);
    }

    @Override
    public void onTeacherSearch(String searchDate, String searchValue) {
        int edtId = edtIdFinder.getEdtIdFromSearchValue(searchValue);
        updateCurrentData(searchDate,"",searchValue,edtId,0,"");
        mapWeekInfo(searchDate,edtId,0);
        clearGridPane(weeklyCalendarView);
        addEventsToView(currentWeekEvents);
    }

    @Override
    public void onClassSearch(String searchDate, String searchValue) {
        int edtId = edtIdFinder.getEdtIdFromSearchValue(searchValue);
        updateCurrentData(searchDate,"",searchValue,edtId,0,"");
        mapWeekInfo(searchDate,edtId,0);
        clearGridPane(weeklyCalendarView);
        addEventsToView(currentWeekEvents);
    }
}
