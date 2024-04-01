package com.example.calendrierceri.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.converter.LocalTimeStringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

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



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
    }
}
