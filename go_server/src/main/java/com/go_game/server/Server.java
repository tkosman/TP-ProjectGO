package com.go_game.server;

import java.io.*;
import java.net.*;
import java.util.Date;

import com.go_game.server.messages.ClientInfoMsg;
import com.go_game.server.messages.IndexSetMsg;

public class Server
{
    //TODO: change if online server is implemented
    private static final int PORT = 4444;
    private static int player_index = 1;

    public static void main(String[] args)
    {
        new Server().runServer();;
    }

    private void runServer()
    {
        try (ServerSocket serverSocket = new ServerSocket(PORT))
        {
            System.out.println(new Date() + "Server started successfully at port " + PORT + "\n");

            while (true)
            {
                //? Wait for a new player to join
                Socket newPlayer = serverSocket.accept();
                //? Create a new output for the newly joined player
                ObjectOutputStream newPlayerOut = new ObjectOutputStream(newPlayer.getOutputStream());
                newPlayerOut.writeObject(new IndexSetMsg(player_index));
                player_index++; //? Increment player index

                System.out.println(new Date() + "\nPlayer joined session with IP address " + newPlayer.getInetAddress().getHostAddress() + "\n");

                ObjectInputStream newPlayerIn = new ObjectInputStream(newPlayer.getInputStream());
                try
                { 
                    ClientInfoMsg newPlayerInfo = (ClientInfoMsg)newPlayerIn.readObject();
                    System.out.println(newPlayerInfo);

                    //TODO: choose game mode
                    //TODO: lookup for a player with the same game mode and pair them, if no player with the same game mode is found, add this player to the queue
                    //TODO: If paired with another player, start a new thread for this game session


                }
                catch (ClassNotFoundException e) { e.printStackTrace(); }





                newPlayerIn.close();
            }



            // int sessionNo = 1;
            // while (true) {
            //     System.out.println(new Date() + "\nWaiting for players to join session " + sessionNo + "\n");

            //     Socket firstPlayer = serverSocket.accept();
            //     System.out.println(new Date() + "\nPlayer 1 joined session " + sessionNo + "with IP address" + firstPlayer.getInetAddress().getHostAddress() + "\n");
            //     new DataOutputStream(firstPlayer.getOutputStream()).writeInt(PLAYER1);

            //     Socket secondPlayer = serverSocket.accept();
            //     System.out.println(new Date() + "\nPlayer 2 joined session " + sessionNo + "with IP address" + secondPlayer.getInetAddress().getHostAddress() + "\n");
            //     new DataOutputStream(secondPlayer.getOutputStream()).writeInt(PLAYER2);

            //     System.out.println(new Date() + "\nStart a thread for session " + sessionNo++ + "\n");
            //     //! Here start game thread for this session -> GameLogicThread
            // }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
