package com.example.calendrierceri.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FilterPopulator {

    String url = "jdbc:mysql://localhost:3306/edt";
    String username = "root";
    String password = "Smail@10";
    private Connection connection;

    public FilterPopulator() throws SQLException {
        connection = DriverManager.getConnection(url, username, password);
    }
    public List<String> getMatiereList(int edtId,int personalEdtId) {

        String sql;
        PreparedStatement statement;
        List<String> matieres = new ArrayList<>();

        if (personalEdtId == 0) {
            sql = "SELECT DISTINCT matiere FROM events WHERE edt_id = ?";
        } else {
            sql = "SELECT DISTINCT matiere FROM events WHERE (edt_id = ? OR edt_id = ?)";
        }

        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, edtId);

            // Si edtExtraId n'est pas égal à zéro, nous avons une quatrième condition
            if (personalEdtId != 0) {
                statement.setInt(2, personalEdtId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    String matiere = resultSet.getString("matiere");
                    if (matiere != null && !matiere.isEmpty()) {
                        matieres.add(matiere);
                    }
                }

            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return matieres;
    }

    public List<String> getTypeList(int edtId,int personalEdtId) {

        String sql;
        PreparedStatement statement;
        List<String> types = new ArrayList<>();

        if (personalEdtId == 0) {
            sql = "SELECT DISTINCT type FROM events WHERE edt_id = ?";
        } else {
            sql = "SELECT DISTINCT type FROM events WHERE (edt_id = ? OR edt_id = ?)";
        }

        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, edtId);

            // Si edtExtraId n'est pas égal à zéro, nous avons une quatrième condition
            if (personalEdtId != 0) {
                statement.setInt(2, personalEdtId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    String type = resultSet.getString("type");
                    if (type != null && !type.isEmpty()) {
                        types.add(type);
                    }
                }

            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return types;
    }

    public List<String> getSalleList(int edtId,int personalEdtId) {

        String sql;
        PreparedStatement statement;
        List<String> salles = new ArrayList<>();

        if (personalEdtId == 0) {
            sql = "SELECT DISTINCT salle FROM events WHERE edt_id = ?";
        } else {
            sql = "SELECT DISTINCT salle FROM events WHERE (edt_id = ? OR edt_id = ?)";
        }

        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, edtId);

            // Si edtExtraId n'est pas égal à zéro, nous avons une quatrième condition
            if (personalEdtId != 0) {
                statement.setInt(2, personalEdtId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    String salle = resultSet.getString("salle");
                    if (salle != null && !salle.isEmpty()) {
                        salles.add(salle);
                    }
                }

            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salles;
    }
}