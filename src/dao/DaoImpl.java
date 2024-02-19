package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.Card;
import model.Player;

public class DaoImpl implements Dao {
    
    // Constants for database connection
    public static final String SCHEMA_NAME = "uno";
    public static final String CON = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    public static final String CONNECTION =
            "jdbc:mysql://localhost:3306/" +
                    SCHEMA_NAME +
                    CON;

    // Database connection parameters
    private Connection connSQL;
    private final static String HOST = "127.0.0.1";
    private final static Integer PORT = 3306;
    private final static String DBNAME = "uno";
    private final static String DBUSER = "root";
    private final static String DBPWD = "";
    
    // Method to establish connection to the database
    @Override
    public void connect() throws SQLException {
        try {
            System.out.println("Establishing connection...");
            
            connSQL = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + PORT + "/" + DBNAME + CON, DBUSER, DBPWD);
            System.out.println("CONNECTION ESTABLISHED!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Method to disconnect from the database
    @Override
    public void disconnect() throws SQLException {
        // Close the connection if it's open
        if (connSQL != null && !connSQL.isClosed()) {
            connSQL.close();
            System.out.println("CONNECTION CLOSED!");
        }
    }

    // Method to get the last ID of a card for a specific player
    @Override
    public int getLastIdCard(int playerId) throws SQLException {
        int lastId = 0;
        try (PreparedStatement statement = connSQL.prepareStatement("SELECT IFNULL(MAX(id), 0) + 1 AS last_id FROM uno_card WHERE id_player = ?")) {
            statement.setInt(1, playerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    lastId = resultSet.getInt("last_id");
                }
            }
        }
        return lastId;
    }

    // Method to get the last card played in the game
    @Override
    public Card getLastCard() throws SQLException {
        Card lastCard = null;
        try (PreparedStatement statement = connSQL.prepareStatement("SELECT * FROM uno_card WHERE id = (SELECT id_card FROM uno_game ORDER BY id DESC LIMIT 1)")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    lastCard = new Card(
                            resultSet.getInt("id"),
                            resultSet.getString("number"),
                            resultSet.getString("color"),
                            resultSet.getInt("id_player")
                    );
                }
            }
        }
        return lastCard;
    }

    // Method to retrieve player information based on username and password
    @Override
    public Player getPlayer(String user, String pass) throws SQLException {
        Player player = null;
        try (PreparedStatement statement = connSQL.prepareStatement("SELECT * FROM uno_player WHERE user = ? AND password = ?")) {
            statement.setString(1, user);
            statement.setString(2, pass);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    player = new Player(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getInt("games"),
                            resultSet.getInt("victories")
                    );
                }
            }
        }
        return player;
    }

    // Method to retrieve all cards belonging to a player
    @Override
    public ArrayList<Card> getCards(int playerId) throws SQLException {
        ArrayList<Card> playerCards = new ArrayList<>();
        try (PreparedStatement statement = connSQL.prepareStatement("SELECT * FROM uno_card WHERE id_player = ?")) {
            statement.setInt(1, playerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Card card = new Card(
                            resultSet.getInt("id"),
                            resultSet.getString("number"),
                            resultSet.getString("color"),
                            playerId
                    );
                    playerCards.add(card);
                }
            }
        }
        return playerCards;
    }

    // Method to retrieve a specific card by its ID
    @Override
    public Card getCard(int cardId) throws SQLException {
        Card card = null;
        try (PreparedStatement statement = connSQL.prepareStatement("SELECT * FROM uno_card WHERE id = ?")) {
            statement.setInt(1, cardId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    card = new Card(
                            resultSet.getInt("id"),
                            resultSet.getString("number"),
                            resultSet.getString("color"),
                            resultSet.getInt("id_player")
                    );
                }
            }
        }
        return card;
    }

    // Method to save a played card to the game history
    @Override
    public void saveGame(Card card) throws SQLException {
        try (PreparedStatement statement = connSQL.prepareStatement("INSERT INTO uno_game (id_card) VALUES (?)")) {
            statement.setInt(1, card.getId());
            statement.executeUpdate();
        }
    }

    // Method to save a new card to the database
    @Override
    public void saveCard(Card card) throws SQLException {
        try (PreparedStatement statement = connSQL.prepareStatement("INSERT INTO uno_card (id, id_player, number, color) VALUES (?, ?, ?, ?)")) {
            statement.setInt(1, card.getId());
            statement.setInt(2, card.getPlayerId());
            statement.setString(3, card.getNumber());
            statement.setString(4, card.getColor());
            statement.executeUpdate();
        }
    }

    // Method to delete a card from the game history
    @Override
    public void deleteCard(Card card) throws SQLException {
        try (PreparedStatement statement = connSQL.prepareStatement("DELETE FROM uno_game WHERE id_card = ?")) {
            statement.setInt(1, card.getId());
            statement.executeUpdate();
        }
    }

    // Method to clear all cards from a player's deck
    @Override
    public void clearDeck(int playerId) throws SQLException {
        try (PreparedStatement statement = connSQL.prepareStatement("DELETE FROM uno_card WHERE id_player = ?")) {
            statement.setInt(1, playerId);
            statement.executeUpdate();
        }
    }

    // Method to increment the number of victories for a player
    @Override
    public void addVictories(int playerId) throws SQLException {
        try (PreparedStatement statement = connSQL.prepareStatement("UPDATE uno_player SET victories = victories + 1 WHERE id = ?")) {
            statement.setInt(1, playerId);
            statement.executeUpdate();
        }
    }

    // Method to increment the number of games played by a player
    @Override
    public void addGames(int playerId) throws SQLException {
        try (PreparedStatement statement = connSQL.prepareStatement("UPDATE uno_player SET games = games + 1 WHERE id = ?")) {
            statement.setInt(1, playerId);
            statement.executeUpdate();
        }
    }

}
