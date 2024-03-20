package com.example.calendrierceri;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public User getUserByCredentials(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE nom = ? AND mot_de_passe = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setMotDePasse(rs.getString("mot_de_passe"));
                    user.setRole(mapRoleName(rs.getInt("role_id")));
                    return user;
                }
            }
        }
        return null;
    }

    private String mapRoleName(int roleId) {
        if (roleId == 1) {
            return "Professeur";
        } else if (roleId == 2) {
            return "Etudiant";
        }
        return null;
    }

}
