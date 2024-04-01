package com.example.calendrierceri.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EdtIdFinder {
    String url = "jdbc:mysql://localhost:3306/edt_ceri";
    String username = "root";
    String password = "root";
    private Connection connection;

    public EdtIdFinder() throws SQLException {
        connection = DriverManager.getConnection(url, username, password);
    }

    public int getEdtIdFromSearchValue(String searchValue){
        String sql;
        PreparedStatement statement;
        int edtId = -1;

        sql = "SELECT id FROM edt WHERE nom = ?";
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, searchValue);


            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    edtId = resultSet.getInt("id");
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return edtId;

    }
}
