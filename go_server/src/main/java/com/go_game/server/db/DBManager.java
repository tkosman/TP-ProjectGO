package com.go_game.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


//? Singleton
public class DBManager {
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/go_replays?useSSL=false";
    private static final String DB_USERNAME = "server";
    private static final String DB_PASSWORD = "password1234";
    private static Connection connection;

    public DBManager() {
        try  {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("connected to database");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet getAllReplays() throws SQLException {
        String query = """
                SELECT ID, date
                FROM replays
                """;

        System.out.println("getting all replays");

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            return statement.executeQuery();
        }
    }

    public ResultSet getReplayString(int id) throws SQLException {
        String query = """
            SELECT replay_string
            FROM replays
            WHERE ID = ?
            """;

        System.out.println("getting replay string");

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, String.valueOf(id));
            return statement.executeQuery();
        }
    }
}
