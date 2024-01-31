package com.go_game.server;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import shared.db.DBManager;
import shared.db.DBQueuer;
import shared.messages.BoardStateMsg;
import shared.messages.ReplayFetchMsg;

/**
 * The ReplayThread class represents a thread that handles replay functionality in the server.
 * It retrieves game IDs and board states from the database and sends them to the client.
 */
public class ReplayThread implements Runnable {
    private DBQueuer dbQueuer;
    private ClientConnection clientConnection;
    private Thread fred;

    /**
     * Constructs a ReplayThread object with the specified client connection.
     * Initializes the DBQueuer and starts the thread.
     *
     * @param clientConnection the client connection to communicate with the client
     */
    public ReplayThread(ClientConnection clientConnection) 
    {
        this.dbQueuer = new DBQueuer(new DBManager());
        this.clientConnection = clientConnection;
        fred = new Thread(this);
        fred.start();
    }

    /**
     * Runs the replay thread.
     * Sends all game IDs and dates to the client.
     * Receives a game ID from the client.
     * Sends all board states for the specified game ID to the client.
     * Closes the database connection.
     * 
     * Used messages:
     * ReplayFetchMsg - sent to communicate btw client and db
     * 
     * @throws IOException if an I/O error occurs when sending or receiving
     */
    @Override
    public void run()
    {
        try 
        {
            //? Send all game IDs and dates
            Map<Integer, Date> gameIDsAndDates = dbQueuer.getAllGameIDs();
            clientConnection.sendMessage(new ReplayFetchMsg(gameIDsAndDates));

            //? Receive game ID
            ReplayFetchMsg replayFetchMsg = (ReplayFetchMsg)clientConnection.receiveMessage();

            //? Send all board states for that game ID
            List<BoardStateMsg> boardStateMsgs = dbQueuer.retrieveGameStates(replayFetchMsg.getGameID());
            clientConnection.sendMessage(new ReplayFetchMsg(boardStateMsgs));
        }
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        finally
        {
            //? Close database connection
            dbQueuer.dbClose();
        }

    }
}
