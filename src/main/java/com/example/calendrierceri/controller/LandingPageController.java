package com.example.calendrierceri.controller;

import com.example.calendrierceri.dao.UserDAO;
import com.example.calendrierceri.model.User;
import com.example.calendrierceri.util.FilterPopulator;
import com.example.calendrierceri.util.FiltreService;
import com.example.calendrierceri.util.NextPreviousService;
import com.example.calendrierceri.util.SearchService;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.example.calendrierceri.controller.WeeklyCalendarViewController.getDbConnection;


public class LandingPageController  implements Initializable  {
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

    @FXML
    private MenuButton filtreMatiere;

    @FXML
    private MenuButton filtreType;

    @FXML
    private MenuButton filtreSalle;

    @FXML
    private MenuButton searchTypeMenu;

    @FXML
    private TextField searchTypeInput;

    @FXML
    private Button searchButton;

    @FXML
    private Button addPersonalEvent;

    private User currentUser;

    private String currentDisplayedDate;

    private NextPreviousService currentNextPreviousService;

    private FiltreService currentFiltreService;

    private SearchService currentSearchService;

    private FilterPopulator filterPopulator;

    @FXML
    private ToggleButton themeToggle;




    public void initializeUserThemePreference(User user){
        Scene scene = themeToggle.getScene();
        if(user.getUserPreferenceTheme().equals("dark")){
            themeToggle.setText("Dark mode");
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/com/example/calendrierceri/dark-theme.css").toExternalForm());
        }
        else {
            themeToggle.setText("Light mode");
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/com/example/calendrierceri/light-theme.css").toExternalForm());
        }
    }


    public void setCurrentUser(User user) throws SQLException {
        this.currentUser=user;
        if(currentUser.getRole().equals("Etudiant")){
            populateMenuButtons(currentUser.getEdtFormationId(),currentUser.getEdtPersonnelId());
        }else {
            populateMenuButtons(currentUser.getEdtProfId(),currentUser.getEdtPersonnelId());
        }
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

        searchTypeMenu.getItems().clear();
        calendarViewType.getItems().clear();

            MenuItem specialitySearchItem = new MenuItem("Search By Speciality");
            MenuItem teacherSearchItem = new MenuItem("Search By Teacher");
            MenuItem classSearchItem = new MenuItem("Search By Class");

            MenuItem weeklyMenuItem = new MenuItem("Weekly");
            MenuItem dailyMenuItem = new MenuItem("Daily");
            MenuItem monthlyMenuItem = new MenuItem("Monthly");


            searchTypeMenu.getItems().addAll(specialitySearchItem,teacherSearchItem,classSearchItem);

            // Ajouter les éléments de menu au MenuButton
            calendarViewType.getItems().addAll(dailyMenuItem, weeklyMenuItem, monthlyMenuItem);

            LocalDate today = LocalDate.now();

            // Formater la date dans le format "yyyy-MM-dd"
            DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            currentDisplayedDate = today.format(formater);

            updateMonthDisplayed(currentDisplayedDate);

            specialitySearchItem.setOnAction(event ->{
                searchTypeMenu.setText("Search by Speciality");
                searchButton.setOnAction(event1 -> {
                    String searchValue = "edt_formation_"+searchTypeInput.getText().toLowerCase();
                    labelEdtInfo.setText(searchTypeInput.getText().toUpperCase()+" Speciality Calendar.");
                    currentSearchService.onSpecialitySearch(currentDisplayedDate,searchValue);
                });
            });

            teacherSearchItem.setOnAction(event ->{
                searchTypeMenu.setText("Search by Teacher");
                searchButton.setOnAction(event1 -> {
                    String searchValue = "edt_prof_"+searchTypeInput.getText().replace(" ", "_").toLowerCase();
                    labelEdtInfo.setText(searchTypeInput.getText().toUpperCase()+" Teacher Calendar.");
                    currentSearchService.onTeacherSearch(currentDisplayedDate,searchValue);
                });
            });

            classSearchItem.setOnAction(event ->{
                searchTypeMenu.setText("Search by Class");
                searchButton.setOnAction(event1 -> {
                    String searchValue ="edt_salle_"+ searchTypeInput.getText().toLowerCase();
                    labelEdtInfo.setText(searchTypeInput.getText().toUpperCase()+" Class Calendar.");
                    currentSearchService.onClassSearch(currentDisplayedDate,searchValue);
                });
            });

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
                    } else {
                        // Remplacer le contenu existant
                        calendarViewVBox.getChildren().set(0, weeklyView);
                    }
                    currentNextPreviousService = weeklyCalendarViewController;
                    currentFiltreService = weeklyCalendarViewController;
                    currentSearchService = weeklyCalendarViewController;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

        dailyMenuItem.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/calendrierceri/dailyCalendarView.fxml"));
                Node dailyView = loader.load();
                DailyCalendarViewController dailyCalendarViewController = loader.getController();
                dailyCalendarViewController.initializeDailyData(currentUser, LocalDate.now());
                calendarViewVBox.getChildren().setAll(dailyView);

                currentNextPreviousService = dailyCalendarViewController;
                currentFiltreService = dailyCalendarViewController;
                currentSearchService = dailyCalendarViewController;
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
                currentFiltreService = monthlyCalendarViewController;
                currentSearchService = monthlyCalendarViewController;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        addPersonalEvent.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/calendrierceri/addPersonnalEventForm.fxml"));
                Parent addPersonnalEventForm = loader.load();
                AddPersonnalEventFormController addPersonnalEventFormController = loader.getController();

                addPersonnalEventFormController.setCurrentUser(currentUser);

                // Création de la scène pour la fenêtre modale
                Scene modalScene = new Scene(addPersonnalEventForm);

                // Création d'une nouvelle fenêtre modale
                Stage addPersonnalEventFormPopUpStage = new Stage();
                addPersonnalEventFormPopUpStage.setTitle("Add Personnal Event Form Pop Up");
                addPersonnalEventFormPopUpStage.initModality(Modality.APPLICATION_MODAL);
                addPersonnalEventFormPopUpStage.setScene(modalScene);

                addPersonnalEventFormPopUpStage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        themeToggle.setOnAction(event -> {
            Scene scene = themeToggle.getScene();
            String themePreference;
            if(themeToggle.isSelected()) {
                themeToggle.setText("Dark mode");
                scene.getStylesheets().clear();
                scene.getStylesheets().add(getClass().getResource("/com/example/calendrierceri/dark-theme.css").toExternalForm());
                themePreference = "dark";
            } else {
                themeToggle.setText("Light mode");
                scene.getStylesheets().clear();
                scene.getStylesheets().add(getClass().getResource("/com/example/calendrierceri/light-theme.css").toExternalForm());
                themePreference = "light";
            }

            try {
                Connection connection = getDbConnection();
                UserDAO userDAO = new UserDAO(connection);
                userDAO.updateUserThemePreference(currentUser.getUserId(), themePreference);
                currentUser.setUserPreferenceTheme(themePreference);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

    }

    //Fix pour la recherche aprés il faut penser a mettre type de recherche pour faire les recherche suivants l'edt diplayed
    public void populateMenuButtons(int edtId,int personnalEdtId) throws SQLException {
        filterPopulator = new FilterPopulator();

        filtreMatiere.getItems().clear();
        filtreType.getItems().clear();
        filtreSalle.getItems().clear();

        List<String> matieres;
        List<String> types;
        List<String> salles;


        matieres = filterPopulator.getMatiereList(edtId,personnalEdtId);
        salles = filterPopulator.getSalleList(edtId,personnalEdtId);
        types = filterPopulator.getTypeList(edtId,personnalEdtId);

        for(String matiere : matieres){
            MenuItem matiereItem = new MenuItem(matiere);
            matiereItem.setOnAction(event ->{
                if(currentUser.getRole().equals("Etudiant")) {
                    currentFiltreService.onMatiereFiltre(currentDisplayedDate, matiere, currentUser.getEdtFormationId(), currentUser.getEdtPersonnelId());
                }
                else {
                    currentFiltreService.onMatiereFiltre(currentDisplayedDate, matiere, currentUser.getEdtProfId(), currentUser.getEdtPersonnelId());
                }
            });
            filtreMatiere.getItems().add(matiereItem);
        }

        for(String salle : salles){
            MenuItem salleItem = new MenuItem(salle);
            salleItem.setOnAction(event ->{
                if(currentUser.getRole().equals("Etudiant")) {
                    currentFiltreService.onSalleFiltre(currentDisplayedDate, salle, currentUser.getEdtFormationId(), currentUser.getEdtPersonnelId());
                }
                else {
                    currentFiltreService.onSalleFiltre(currentDisplayedDate, salle, currentUser.getEdtProfId(), currentUser.getEdtPersonnelId());
                }
            });
            filtreSalle.getItems().add(salleItem);
        }

        for(String type : types){
            MenuItem typeItem = new MenuItem(type);
            typeItem.setOnAction(event ->{
                if(currentUser.getRole().equals("Etudiant")) {
                    currentFiltreService.onTypeFiltre(currentDisplayedDate, type, currentUser.getEdtFormationId(), currentUser.getEdtPersonnelId());
                }
                else {
                    currentFiltreService.onMatiereFiltre(currentDisplayedDate, type, currentUser.getEdtProfId(), currentUser.getEdtPersonnelId());
                }
            });
            filtreType.getItems().add(typeItem);
        }

    }

}
