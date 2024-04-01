package com.example.calendrierceri.controller;

import com.example.calendrierceri.model.Event;
import com.example.calendrierceri.model.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.converter.LocalTimeStringConverter;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

import static com.example.calendrierceri.controller.WeeklyCalendarViewController.getDbConnection;

public class AddPersonnalEventFormController implements Initializable {

    @FXML
    private Button submitEventButton;

    @FXML
    private TextArea eventDescription;

    @FXML
    private Spinner durationPicker;

    @FXML
    private Spinner hourPicker;

    @FXML
    private DatePicker datePicker;

    private Connection connection;

    private Event currentEvent;

    private User currentUser;


    public void setCurrentUser(User user){
        currentUser = user;
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            connection = getDbConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        datePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isBefore(LocalDate.now()));
            }
        });

        // Limiter les heures dans le Spinner pour les heures entre 6:00 et 18:00
        SpinnerValueFactory<LocalTime> valueFactory = new SpinnerValueFactory<LocalTime>() {
            {
                setConverter(new LocalTimeStringConverter());
                setWrapAround(true);
                setValue(LocalTime.of(6, 0));
            }

            @Override
            public void decrement(int steps) {
                LocalTime time = getValue().minusHours(steps);
                if (time.getHour() >= 6) {
                    setValue(time);
                }
            }

            @Override
            public void increment(int steps) {
                LocalTime time = getValue().plusHours(steps);
                if (time.getHour() <= 18) {
                    setValue(time);
                }
            }
        };

        hourPicker.setValueFactory(valueFactory);

        SpinnerValueFactory<Integer> durationValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 1);
        durationPicker.setValueFactory(durationValueFactory);

        durationPicker.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                LocalTime selectedTime = (LocalTime) hourPicker.getValue();
                LocalTime maxTime = LocalTime.of(18, 0);
                LocalTime endTime = selectedTime.plusHours((int) newValue);
                if (endTime.isAfter(maxTime)) {
                    // Réduire la valeur de la durée si la somme dépasse 18:00

                    durationPicker.getValueFactory().setValue( (int) durationPicker.getValue() - 1);
                }
            }
        });

        submitEventButton.setOnAction(event -> {
            createEventFromFormInput();
            insertEventIntoDatabase(currentEvent,(Stage) datePicker.getScene().getWindow());
        });
    }


    public void createEventFromFormInput(){
        LocalDate selectedDate = datePicker.getValue();
        LocalTime selectedTime = (LocalTime) hourPicker.getValue();
        int duration = (int) durationPicker.getValue();
        String description = eventDescription.getText();

        // Créer la date de début au format requis
        String dtstart = selectedDate.toString() + " " + selectedTime.toString() + ":00";

        // Calculer la date de fin en ajoutant la durée au dtstart
        LocalTime endTime = selectedTime.plusHours(duration);
        String dtend = selectedDate.toString() + " " + endTime.toString() + ":00";

        currentEvent = new Event(dtstart, dtend, "", "", "", "", "Perso", currentUser.getEdtPersonnelId(), description);
    }

    private void insertEventIntoDatabase(Event event, Stage modalStage) {
        // Connexion à votre base de données
        try {
            // Requête SQL pour insérer l'événement
            String sql = "INSERT INTO events (dtstart, dtend, matiere, enseignant, td, salle, type, edt_id, event_description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            // Création de la déclaration préparée avec la requête SQL
            PreparedStatement statement = connection.prepareStatement(sql);
            // Définition des valeurs pour les paramètres de la déclaration préparée
            statement.setString(1, event.getDtstart());
            statement.setString(2, event.getDtend());
            statement.setString(3, event.getMatiere());
            statement.setString(4, event.getEnseignant());
            statement.setString(5, event.getTd());
            statement.setString(6, event.getSalle());
            statement.setString(7, event.getType());
            statement.setInt(8, event.getEdtId());
            statement.setString(9, event.getDescription());
            // Exécution de la déclaration préparée pour insérer l'événement
            int rowsInserted = statement.executeUpdate();

            // Vérifier si l'ajout a réussi
            if (rowsInserted > 0) {
                // Fermer le stage du modal
                modalStage.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
