package com.go_game.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

//! Please write your javaFX client based on this class
//! Keep in mind that this class is just a schema and will be deleted in the future

public class ClientSchema implements Runnable
{
    private static enum typeOfPlayers {PLAYER1, PLAYER2};

    private Socket socket;
    private DataInputStream fromServer;
    private typeOfPlayers whoIsPlayer;

    //TODO: use this variable to determine whose turn is it
    private typeOfPlayers whoseIsTurn;

    public static void main(String[] args)
    {
        new ClientSchema();
    }

    private ClientSchema()
    {
        establishServerConnection();
    }

    private void establishServerConnection()
    {
        try
        {
            socket = new Socket("localhost", 4444); //? in future extended not only to localhost
            fromServer = new DataInputStream(socket.getInputStream());
            // DataOutputStream toServer = new DataOutputStream(socket.getOutputStream());

            int player = fromServer.readInt();
            whoIsPlayer = (player == '1') ? typeOfPlayers.PLAYER1 : typeOfPlayers.PLAYER2;
            System.out.println("You are player " + whoIsPlayer + "\n");

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
}
