package com.go_game.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;

import shared.enums.GameMode;
import shared.messages.ClientInfoMsg;
import shared.messages.IndexSetMsg;
import shared.enums.BoardSize;

public class Server
{
    //TODO: change if online server is implemented
    private static final int PORT = 4444;
    private static int player_index = 1;
    
    ClientConnection clientConnection;
    ClientConnection waitingPlayer9x9Connection;
    ClientConnection waitingPlayer13x13Connection;
    ClientConnection waitingPlayer19x19Connection;

    public static void main(String[] args)
    {
        new Server().runServer();
    }
    
    private void runServer()
    {
        try (ServerSocket serverSocket = new ServerSocket(PORT))
        {
            System.out.println(new Date() + "\nServer started successfully at port " + PORT + "\n");

            while (true)
            {
                //! 1 IN
                //? Wait for a new player to join
                clientConnection = new ClientConnection(serverSocket.accept());

                try
                { 
                    //! 2 OUT
                    clientConnection.sendMessage(new IndexSetMsg(player_index));
                    player_index++; //? Increment player index

                    System.out.println(new Date() + "\nPlayer joined session with IP address " + clientConnection.getSocket().getInetAddress().getHostAddress() + "\n");

                    //! 3 IN
                    ClientInfoMsg newPlayerInfo = (ClientInfoMsg)clientConnection.receiveMessage();
                    System.out.println(newPlayerInfo);

                    GameMode gameMode = newPlayerInfo.getGameMode();
                    if (gameMode == GameMode.REPLAY)
                    {
                        System.out.println("REPLAY");
                        new ReplayThread(clientConnection);
                    }
                    else if (gameMode == GameMode.BOT)
                    {
                        System.out.println("BOT");
                        new BotThread(clientConnection, newPlayerInfo.getBoardSize().toInt());
                    }
                    else if (gameMode == GameMode.MULTI_PLAYER)
                    {
                        BoardSize boardSize = newPlayerInfo.getBoardSize();

                        if(boardSize == BoardSize.NINE_X_NINE)
                        {
                            if(waitingPlayer9x9Connection == null)
                            {
                                waitingPlayer9x9Connection = new ClientConnection(clientConnection.getSocket(), clientConnection.getOutputStream(), clientConnection.getInputStream());
                            }
                            else
                            {
                                new MultiplayerGameThread(clientConnection, waitingPlayer9x9Connection, boardSize.toInt());
                                waitingPlayer9x9Connection = null;
                            }

                        }
                        else if(boardSize == BoardSize.THIRTEEN_X_THIRTEEN)
                        {
                            if(waitingPlayer13x13Connection == null)
                            {
                                waitingPlayer13x13Connection = new ClientConnection(clientConnection.getSocket(), clientConnection.getOutputStream(), clientConnection.getInputStream());
                            }
                            else
                            {
                                new MultiplayerGameThread(clientConnection, waitingPlayer13x13Connection, boardSize.toInt());
                                waitingPlayer13x13Connection = null;
                            }
                        }
                        else if(boardSize == BoardSize.NINETEEN_X_NINETEEN)
                        {
                            if(waitingPlayer19x19Connection == null)
                            {
                                waitingPlayer19x19Connection = new ClientConnection(clientConnection.getSocket(), clientConnection.getOutputStream(), clientConnection.getInputStream());
                            }
                            else
                            {
                                new MultiplayerGameThread(clientConnection, waitingPlayer19x19Connection, boardSize.toInt());
                                waitingPlayer19x19Connection = null;
                            }
                        }
                        else { assert false : "Unknown board size"; }
                    }
                    else { assert false : "Unknown game mode"; }

                }
                catch (ClassNotFoundException e) 
                {
                    System.out.println("Wrong message type received, waited for ClientInfoMsg!");
                    e.printStackTrace(); 
                }
                catch (IOException e) 
                {
                    System.out.println("Player disconnected or coudn't send message.");
                    e.printStackTrace();
                }
            }
        }
        catch (IOException ex)
        {
            System.out.println("Could not start server.");
            ex.printStackTrace();
        }
    }

}
