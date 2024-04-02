package com.example.calendrierceri.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Alert.AlertType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class BookingFormController {

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> timeSlotComboBox;

    @FXML
    private ComboBox<String> roomComboBox;

    private Connection connect() {
        String url = "jdbc:mysql://localhost:3306/edt";
        String username = "root";
        String password = "Smail@10";
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
    }

    private void populateTimeSlots() {
        timeSlotComboBox.getItems().addAll("08:30-10:00", "10:00-11:30", "11:30-13:00", "13:00-14:30", "14:30-16:00", "16:00-17:30", "17:30-19:00");
    }

    private void populateRooms() {
        roomComboBox.getItems().addAll("edt_salle_stat1", "edt_salle_stat6");
    }

    @FXML
    private void bookRoom() {
        LocalDate date = datePicker.getValue();
        String timeSlot = timeSlotComboBox.getValue();
        String room = roomComboBox.getValue();

        if (date == null || timeSlot == null || room == null) {
            showAlert("Booking Error", "Please select date, time slot, and room.", AlertType.ERROR);
            return;
        }

        // Nouvelle étape : récupérer l'edt_id basé sur la salle (vous devez implémenter la logique correspondante)
        Integer edtId = getEdtIdFromRoom(room);
        if (edtId == null) {
            showAlert("Internal Error", "Could not determine the timetable for the selected room.", AlertType.ERROR);
            return;
        }

        if (checkAvailability(date, timeSlot, room)) {
            insertBooking(date, timeSlot, room, edtId); // Notez l'ajout de edtId comme argument
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
    private void insertBooking(LocalDate date, String timeSlot, String room, Integer edtId) {
        String[] times = timeSlot.split("-");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime startTime = LocalTime.parse(times[0], dtf);
        LocalTime endTime = LocalTime.parse(times[1], dtf);

        String insertQuery = "INSERT INTO events (dtstart, dtend, matiere, enseignant, salle, type, td, edt_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"; // Notez l'ajout de 'type' et 'td'
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setString(1, date.atTime(startTime).toString());
            pstmt.setString(2, date.atTime(endTime).toString());
            pstmt.setString(3, "Anglais"); // Ces valeurs devraient être dynamiques selon votre cas d'utilisation
            pstmt.setString(4, "Carole Rey");
            pstmt.setString(5, room);
            pstmt.setString(6, "TD"); // Ajoutez 'type'
            pstmt.setString(7, "M1-ILSEN-cla-GR1"); // Ajoutez 'td'
            pstmt.setInt(8, edtId); // Assurez-vous que cette valeur est correctement définie

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
