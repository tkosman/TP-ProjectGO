package shared.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;


//? Singleton
public class DBManager {
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/go_replays?useSSL=false";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "root";
    private static Connection connection;

    public DBManager() {
        try  {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("connected to database");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveGameState(int gameID, int moveNumber, byte[] gameState) throws SQLException {
        String query = "INSERT INTO replays (game_id, move_number, state, date) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, gameID);
            statement.setInt(2, moveNumber);
            statement.setBytes(3, gameState);
            statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            statement.executeUpdate();
        }
    }

    public List<byte[]> getGameStates(int gameID) throws SQLException 
    {
        List<byte[]> gameStates = new ArrayList<>();
        String query = "SELECT state FROM replays WHERE game_id = ? ORDER BY move_number";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, gameID);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    byte[] state = resultSet.getBytes("state");
                    gameStates.add(state);
                }
            }
        }
        return gameStates;
    }

    public int getHighestGameNumber() throws SQLException {
        String query = "SELECT MAX(game_id) AS max_game_id FROM replays";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("max_game_id");
                } else {
                    return 0; // Default value if no games are found
                }
            }
        }
    }

    
}
