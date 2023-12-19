package com.go_game.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientSchema
{
    private static enum typeOfPlayers {PLAYER1, PLAYER2};

    private Socket socket;
    private DataInputStream fromServer;
    private typeOfPlayers whoIsPlayer;

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
        }
        catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
