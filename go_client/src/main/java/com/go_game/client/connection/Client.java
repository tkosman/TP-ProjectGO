package com.go_game.client.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import shared.enums.BoardSize;
import shared.enums.GameMode;
import shared.enums.Stone;
import shared.messages.ClientInfoMsg;
import shared.messages.GameJoinedMsg;
import shared.messages.IndexSetMsg;
import shared.messages.OkMsg;
import shared.messages.StringMsg;


public class Client implements Runnable
{
    private final static String HOST = "localhost";//? in future extended not only to localhost
    private final static int PORT = 4444;

    private Socket socket;
    private ObjectInputStream fromServer;
    private ObjectOutputStream toServer;
    private int gameID;
    private Stone playerColor;
    private boolean isPlayer1Turn;


    public static void main(String[] args) throws ClassNotFoundException
    {
        new Client();
    }

    public Client() throws ClassNotFoundException
    {
        establishServerConnection();
    }

    private void establishServerConnection() throws ClassNotFoundException
    {
        try
        {
            //! 1 OUT
            socket = new Socket(HOST, PORT);
            fromServer = new ObjectInputStream(socket.getInputStream());
            toServer = new ObjectOutputStream(socket.getOutputStream());

            //! 2 IN
            //? Get player index from server
            IndexSetMsg playerIndex = (IndexSetMsg)fromServer.readObject();
            System.out.println("You are player " + playerIndex.getIndex() + "\n");

            //! 3 OUT
            toServer.writeObject(new ClientInfoMsg(BoardSize.NINE_X_NINE, GameMode.MULTI_PLAYER));

            //! 4 IN
            GameJoinedMsg gameJoinedMsg = (GameJoinedMsg)fromServer.readObject();
            gameID = gameJoinedMsg.getGameID();
            playerColor = gameJoinedMsg.getStoneColor();
            isPlayer1Turn = gameJoinedMsg.isPlayer1Turn();

            //! HANDSHAKE FINISHED
            Thread fred = new Thread(this);
            fred.start();
        }
        catch (IOException ex) {
            System.err.println(ex);
        }
    }
    @Override
    public void run()
    {
        //TODO: add some condition to stop this loop
        while (true)
        {
            
            //? this is a basic game flow that definitely will be changed
            if (whoIsPlayer == whoseIsTurn)
            {
                receiveInfoFromServer();
                someKindOfWaiting();
                sendMoveToServer();
            }
            else
            {
                receiveInfoFromServer();
                someKindOfWaiting();
                receiveOpponentMove();
            }
        }
    }
    
    /*
    //TODO: implement this method
    private void receiveOpponentMove()
    {
        //? This method will be responsible for receiving opponent move
    }

    //TODO: implement this method
    private void receiveInfoFromServer()
    {
        //? This method will be responsible for receiving info from server
    }

    //TODO: implement this method
    private void sendMoveToServer()
    {
        //? This method will be responsible for sending move to server
    }

    //ToDO: implement this method
    private void someKindOfWaiting()
    {
        //? This method will be responsible for waiting for opponent move
    }
    */
}
