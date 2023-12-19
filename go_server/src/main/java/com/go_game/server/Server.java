package com.go_game.server;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class Server
{
    public static final int PLAYER1 = 1;
    public static final int PLAYER2 = 2;

    public static void main(String[] args)
    {
        new Server();
    }

    private Server()
    {
        try (ServerSocket serverSocket = new ServerSocket(4444)) {
            System.out.println(new Date() + "Server started at port 4444\n");

            int sessionNo = 1;
            while (true) {
                System.out.println(new Date() + "\nWaiting for players to join session " + sessionNo + "\n");

                Socket firstPlayer = serverSocket.accept();
                System.out.println(new Date() + "\nPlayer 1 joined session " + sessionNo + "with IP address" + firstPlayer.getInetAddress().getHostAddress() + "\n");
                new DataOutputStream(firstPlayer.getOutputStream()).writeInt(PLAYER1);

                Socket secondPlayer = serverSocket.accept();
                System.out.println(new Date() + "\nPlayer 2 joined session " + sessionNo + "with IP address" + secondPlayer.getInetAddress().getHostAddress() + "\n");
                new DataOutputStream(secondPlayer.getOutputStream()).writeInt(PLAYER2);

                System.out.println(new Date() + "\nStart a thread for session " + sessionNo++ + "\n");
                //! Here start game thread for this session
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
