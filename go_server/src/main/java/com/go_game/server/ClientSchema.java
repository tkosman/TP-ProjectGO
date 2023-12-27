package com.go_game.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.go_game.server.messages.ClientInfoMsg;
import com.go_game.server.messages.IndexSetMsg;
import com.go_game.server.messages.OkMsg;

import main.java.com.go_game.server.enums.BoardSize;

import com.go_game.server.enums.GameMode;

//! Please write your javaFX client based on this class
//! Keep in mind that this class is just a schema and will be deleted in the future

public class ClientSchema
{
    private final static String HOST = "localhost";//? in future extended not only to localhost
    private final static int PORT = 4444;

    private Socket socket;
    private ObjectInputStream fromServer;
    private ObjectOutputStream toServer;

    public static void main(String[] args) throws ClassNotFoundException
    {
        new ClientSchema();
    }

    private ClientSchema() throws ClassNotFoundException
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

            //! for debuging puproses
            System.out.println((OkMsg)fromServer.readObject());

            // Thread fred = new Thread(this);
            // fred.start();
        }
        catch (IOException ex) {
            System.err.println(ex);
        }
    }

    /*
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
    */
}
