package com.example.calendrierceri.controller;

import com.example.calendrierceri.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class BookingFormController {

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> timeSlotComboBox;

    @FXML
    private ComboBox<String> roomComboBox;

    @FXML
    private TextField subjectText;

    @FXML
    private TextField promotionText;

    @FXML
    private ComboBox<String> classReservationType;

    private User currentUser;


    public void setCurrentUser(User user){
        currentUser = user;
    }

    private Connection connect() {
        String url = "jdbc:mysql://localhost:3306/edt_ceri";
        String username = "root";
        String password = "root";
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    public void initialize() {
        populateTimeSlots();
        populateRooms();
        populateTypes();
        datePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isBefore(LocalDate.now()));
            }
        });
    }

    private void populateTimeSlots() {
        timeSlotComboBox.getItems().addAll("08:00-09:30", "09:30-11:00", "11:00-12:30", "12:30-14:00", "14:00-15:30", "15:30-17:00", "17:00-18:30");
    }

    private void populateRooms() {
        roomComboBox.getItems().addAll("Stat 1 = info - C137", "Stat 6 = info - C129");
    }

    private void populateTypes() {
        classReservationType.getItems().addAll("TP", "Evaluation","CM","TD");
    }

    @FXML
    private void bookRoom() {
        LocalDate date = datePicker.getValue();
        String timeSlot = timeSlotComboBox.getValue();
        String room = roomComboBox.getValue();
        String type = classReservationType.getValue();
        String promotions = promotionText.getText();
        String subject = promotionText.getText();

        if (date == null || timeSlot == null || room == null || type == null || promotions == null || subject == null) {
            showAlert("Booking Error", "Please select all the fields before submitting.", AlertType.ERROR);
            return;
        }

        // Nouvelle étape : récupérer l'edt_id basé sur la salle (vous devez implémenter la logique correspondante)
        Integer edtId = getEdtIdFromRoom(room);
        if (edtId == null) {
            showAlert("Internal Error", "Could not determine the timetable for the selected room.", AlertType.ERROR);
            return;
        }

        if (checkAvailability(date, timeSlot, room)) {
            insertBooking(date, timeSlot, room, edtId,type,subject,promotions); // Notez l'ajout de edtId comme argument
        } else {
            showAlert("Booking Unavailable", "Selected room and time slot is not available.", AlertType.INFORMATION);
        }
    }

    // Cette méthode est hypothétique. Vous devez implémenter la logique pour récupérer l'edt_id basé sur la salle
    private Integer getEdtIdFromRoom(String room) {
        System.out.println("Recherche de l'edt_id pour la salle: " + room); // Log pour débogage
        String query = "SELECT id FROM edt WHERE nom = ?"; // Correction ici
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, room);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Integer edtId = rs.getInt("id");
                System.out.println("edt_id trouvé: " + edtId); // Log pour débogage
                return edtId;
            } else {
                System.out.println("Aucun edt_id trouvé pour la salle: " + room); // Log d'erreur
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Si l'edt_id n'est pas trouvé, retourner null
    }



    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean checkAvailability(LocalDate date, String timeSlot, String room) {
        String[] times = timeSlot.split("-");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime startTime = LocalTime.parse(times[0], dtf);
        LocalTime endTime = LocalTime.parse(times[1], dtf);

        String query = "SELECT COUNT(*) FROM events WHERE dtstart < ? AND dtend > ? AND td LIKE ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, date.atTime(endTime).toString());
            pstmt.setString(2, date.atTime(startTime).toString());
            pstmt.setString(3, "%" + room + "%");

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0; // Room is available if count is 0
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    private void insertBooking(LocalDate date, String timeSlot, String room, Integer edtId,String type, String subject, String promotions) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Analyser la chaîne de caractères de date en un objet LocalDateTime
        String dateString = date.format(formatter);

        String[] times = timeSlot.split("-");
        String startTime = dateString+" "+times[0];
        String endTime = dateString+" "+times[1];

        String insertQuery = "INSERT INTO events (dtstart, dtend, matiere, enseignant, salle, type, td, edt_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setString(1, startTime+":00");
            pstmt.setString(2, endTime+":00");
            pstmt.setString(3, subject);
            pstmt.setString(4, currentUser.getPrenom()+" "+currentUser.getNom());
            pstmt.setString(5, room);
            pstmt.setString(6, type);
            pstmt.setString(7, promotions);
            pstmt.setInt(8, edtId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert("Booking Success", "The room has been successfully booked.", AlertType.INFORMATION);
            } else {
                showAlert("Booking Error", "An error occurred during booking.", AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}